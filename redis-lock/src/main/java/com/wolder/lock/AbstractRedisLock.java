package com.wolder.lock;

import cn.hutool.core.thread.AsyncUtil;
import com.wolder.config.MessageReceiver;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonBaseLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * description 抽象类
 *
 * @author: woldier
 * @date: 2023/7/28 上午11:16
 */
@Getter
@Slf4j
public abstract class AbstractRedisLock implements RLock {
    protected static String id = UUID.randomUUID().toString();  //UUID  保证多次创建 实例id 一样的


    protected final Long threadId; //线程id
    protected final String name;  //业务名称
    protected final StringRedisTemplate stringRedisTemplate;  //redis请求
    protected final MessageReceiver messageReceiver;  //订阅消息监听器


    protected final static Long DEFAULT_LESS_TIME = 30_000L; //默认过期时间
    protected final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;  //默认过期时间的单位
    /**
     * 存放续期任务的map
     * key是业务字段
     * 存储的对象中包含了当前线程id
     */
    private static final ConcurrentMap<String, ExpirationEntry> EXPIRATION_RENEWAL_MAP = new ConcurrentHashMap<>();  //用于保存本地中获取了业务锁的实体类

    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();  //定时任务

    /*
     * 尝试获取锁方法
     * */
    private final static RedisScript<Long> RENEW_EXPIRE = RedisScript.of(
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return 1; " +
                    "end; " +
                    "return 0;"
            , Long.class);

    /**
     * description 续期entity 类
     * 配合 EXPIRATION_RENEWAL_MAP 使用 , 第一次进入lock 时EXPIRATION_RENEWAL_MAP 中为空  那么需要创建一个entity 放入map中
     *
     * @author: woldier
     * @date: 2023/7/29 上午10:25
     */
    public static class ExpirationEntry {
        /*
         *
         * */
        private final Map<Long, Integer> threadIds = new LinkedHashMap<>();  //保存线程id

        public ExpirationEntry() {
            super();
        }

        /**
         * 添加线程 id
         *
         * @param threadId
         */
        public synchronized void addThreadId(long threadId) {
            threadIds.compute(threadId, (t, counter) -> {
                counter = Optional.ofNullable(counter).orElse(0); //如果计数值为null,那么就赋值默认值0
                counter++; //count 自增
                return counter; //返回自增后的值
            });
        }

        /**
         * description 是否有对应线程id
         *
         * @return 返回true 表示有,返回false 表示没有
         * @author: woldier
         * @date: 2023/7/29 上午10:48
         */
        public synchronized boolean hasNoThreads() { //是否有线程
            return threadIds.isEmpty();
        }

        /**
         * description 返回threadIdsmap中的第一个
         *
         * @return
         * @author: woldier
         * @date: 2023/7/29 上午10:50
         */
        public synchronized Long getFirstThreadId() { //获取list中的第一个
            if (threadIds.isEmpty()) {
                return null;
            }
            return threadIds.keySet().iterator().next();
        }

        public synchronized void removeThreadId(long threadId) {
            threadIds.compute(threadId, (t, counter) -> {
                if (counter == null) { //如果key不存在 count 就为null 此时直接return null
                    return null;
                }
                counter--; //count 不是null 那么就进行 自减
                if (counter == 0) { //如果自减后计数为0 那么就 返回null
                    return null;
                }
                return counter; //返回自减后的值
            });
        }
    }

    public AbstractRedisLock(Long threadId, String name, StringRedisTemplate stringRedisTemplate, MessageReceiver messageReceiver) {

        this.threadId = threadId;
        if (name.startsWith("lick:"))  //给业务锁都
            this.name = name;
        else
            this.name = "lock:" + name;
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageReceiver = messageReceiver;
    }

    /**
     * description 返回redis中的key
     *
     * @return 返回redis中的key
     * @author: woldier
     * @date: 2023/7/28 上午11:31
     */
    public String getHashKeyName() {
        return id + ":" + threadId;
    }

    public String getHashKeyName(Long threadId) {
        return id + ":" + threadId.toString();
    }
    /**
     *
     * description 尝试加锁
     * return: 如果加锁成功返回null,如果不成功说明其他线程已经加锁,返回剩余过期时间
     * @author: woldier
     * @date: 2023/7/29 上午8:24
     */


    /**
     * description 启动一个异步线程
     *
     * @author: woldier
     * @date: 2023/7/29 上午8:38
     */
    protected <S> CompletableFuture<S> supplyAsync(Supplier<S> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * description 获取异步线程的执行返回
     *
     * @author: woldier
     * @date: 2023/7/29 上午8:39
     */
    protected <R> R get(CompletableFuture<R> completableFuture) {
        return AsyncUtil.get(completableFuture);
    }

    /**
     * description 启动
     *
     * @author: woldier
     * @date: 2023/7/29 上午11:13
     */
    protected void watchDogSchedule() {
        ExpirationEntry entry = new ExpirationEntry(); //创建一个entity
        ExpirationEntry oldEntry = EXPIRATION_RENEWAL_MAP.putIfAbsent(getName(), entry); //如果不存在才会进行put操作
         if (oldEntry != null) {
            //说明已经存在    这里还需要想明白一个问题 如果第二次重入 是在过期时间还有25s时重入的,那么 此时过期时间被刷新成30s ,然后5s后会进行看门狗刷新过期时间又变成了30s ,这会打破1/3时间刷新吗,当然是不会的,因为再下次调用也是10s后
            log.info(getHashKeyName() + " ExpirationEntry已经存在,不需要再新建");
            oldEntry.addThreadId(getThreadId());
        } else { //说明是第一次 需要开启看门狗
            log.info(getHashKeyName() + " ExpirationEntry不存在,需要新建");
            entry.addThreadId(getThreadId());
            watchDog();
        }


    }

    private void watchDog() {
        ExpirationEntry entry = EXPIRATION_RENEWAL_MAP.get(getName()); //获取实体
        if (entry == null) { //说明线程已经解锁
            log.info(" entry为空,watchdog 结束");
            return; //结束看门狗
        }
        hashedWheelTimer.newTimeout(

                timeout -> { //定时器执行逻辑
                    ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getName()); //获取实体
                    if (ent == null) {
                        log.info(getHashKeyName() + " entity不存在了,watch_dog退出");
                        return;
                    } //结束watch dog
                    Long id = ent.getFirstThreadId();
                    if (id == null) { //获取线程id 为null 说明以及没有线程加锁
                        log.info(getHashKeyName() + " 当前线程id为空,watchdog退出");
                        return;
                    }
//                    if(!id.equals(getThreadId())){
//                        log.info(getHashKeyName() + " 当前watchdog过期");
//                        return;
//                    }
                    log.info(getHashKeyName(id) + " 启动watch dog..............");
                    //做续期
                    CompletableFuture<Long> rewNewTask = supplyAsync(
                            () -> getStringRedisTemplate().execute(
                                    RENEW_EXPIRE,
                                    Collections.singletonList(getName()),
                                    String.valueOf(DEFAULT_LESS_TIME),
                                    getHashKeyName(id)));
                    rewNewTask.whenComplete((status, e) -> { //完成后判断状态字
                        if (e != null) {
                            //说明报错了
                            EXPIRATION_RENEWAL_MAP.remove(getName());
                            log.error(getHashKeyName(id) + " 执行renew操作的时候报错了", e);
                        }
                        if (status == 0) {//退出
                            log.info(getHashKeyName(id)+" 返回0说明当前续期失败");
//                          watchDog();
                            return;
                            //cancelReNew(null);
                        } else {
                            log.info(getHashKeyName(id)+ " 执行完续期任务递归调用");
                            watchDog(); //重新调用
                        }

                    });
                }, DEFAULT_LESS_TIME / 3, DEFAULT_TIME_UNIT
        );


    }

    /**
     * description 取消wat
     * 这里可能存在两种情况,一种时锁重入的退出,此时只需要计数减一情况
     * * 还有一种情况是修存在错误了 EXPIRATION_RENEWAL_MAP
     *
     * @author: woldier
     * @date: 下午12:56
     */
    private void cancelReNew(Long threadId) {
        ExpirationEntry entry = EXPIRATION_RENEWAL_MAP.get(getName());
        if (entry == null) return;
        if (threadId != null) { //重入锁退出情况
            entry.removeThreadId(threadId);
        }
        if (threadId == null && entry.hasNoThreads()) { //如果是threadId传入null,并且是当前entity重入次数已经是0了 那么就从map中remove
            EXPIRATION_RENEWAL_MAP.remove(getName());
        }
    }

    protected void tryRelease(RedisScript<Long> redisScript) {
        try {
            supplyAsync(() ->
                    getStringRedisTemplate().execute(redisScript,
                            Arrays.asList(getName(), "publish_channel_lock"),
                            getName(),//ARGV[1]发布的消息内容
                            String.valueOf(DEFAULT_LESS_TIME),//过期时间
                            getHashKeyName() //ARGV[3]锁名称
                    )
            ).handle((res, e) -> {
                log.info(getHashKeyName() + " 解锁");
                if (e != null) {
                    log.error("解锁出现错误", e);
                }
                if (res == null) throw new IllegalMonitorStateException(getHashKeyName() + " 没有加锁,不能进行解锁");
                cancelReNew(getThreadId()); //重入次数减一
                if (res == 0L) {
                    log.info(getHashKeyName() + " 重入次数减一");
                } else {
                    //重入次数变为0,删除map来停止看门狗
                    log.info(getHashKeyName() + " 重入次数为0,通知看门狗停止");
//                    EXPIRATION_RENEWAL_MAP.remove(getName());
//                    getStringRedisTemplate().execute(RedisScript.of("redis.call('PUBLISH',KEYS[1],ARGV[1])"),Collections.singletonList("publish_channel_lock"),getName());
                }
                return res;
            }).get();
        } catch (InterruptedException | ExecutionException | IllegalMonitorStateException e) {
            throw new RuntimeException(e);
        }
    }
}
