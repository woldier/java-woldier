package com.wolder.lock;

import com.wolder.config.MessageReceiver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * description TODO
 *
 * @author: woldier
 * @date: 2023/7/31 上午10:34
 */
@Slf4j
@Data
public class RedisFairLock extends AbstractRedisLock {
    private final long threadWaitTime;
    private final static RedisScript<Long> TRY_LOCK = RedisScript.of(
            // remove stale threads
            "while true do " +
                    "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);" + //获取队头元素的线程标识 UUID:threadId
                    "if firstThreadId2 == false then " + // 如果队头元素为false 那么说明队列为空 跳出循环
                    "break;" +
                    "end;" +

                    "local timeout = tonumber(redis.call('zscore', KEYS[3], firstThreadId2));" + //从zset中拿到超时时间
                    "if timeout <= tonumber(ARGV[4]) then " + //如果超时时间小于当前时间 那么就将其删除
                    // remove the item from the queue and timeout set
                    // NOTE we do not alter any other timeout
                    "redis.call('zrem', KEYS[3], firstThreadId2);" + //从zset中删除
                    "redis.call('lpop', KEYS[2]);" + //从queue中删除
                    "else " +
                    "break;" + //当前队头元素没有过期 那么就停止循环
                    "end;" +
                    "end;" +

                    // check if the lock can be acquired now
                    "if (redis.call('exists', KEYS[1]) == 0) " + //如果当前业务key不存在 并且 (队列中没有元素 或者 队列头部的元素与当前请求加锁的线程表示一致)
                    "and ((redis.call('exists', KEYS[2]) == 0) " +
                    "or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then " +

                    // remove this thread from the queue and timeout set
                    "redis.call('lpop', KEYS[2]);" + //
                    "redis.call('zrem', KEYS[3], ARGV[2]);" +

                    // decrease timeouts for all waiting in the queue
                    "local keys = redis.call('zrange', KEYS[3], 0, -1);" +
                    "for i = 1, #keys, 1 do " +
                    "redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);" +  //给所有的key 减一个超时时间

                    "end;" +
                    // acquire the lock and set the TTL for the lease
                    "redis.call('hset', KEYS[1], ARGV[2], 1);" +  //设置锁
                    "redis.call('pexpire', KEYS[1], ARGV[1]);" +  //设置超时时间
                    "return nil;" +
                    "end;" +

                    // check if the lock is already held, and this is a re-entry
                    "if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then " +  //锁重入情况
                    "redis.call('hincrby', KEYS[1], ARGV[2],1);" +
                    "redis.call('pexpire', KEYS[1], ARGV[1]);" +
                    "return nil;" +
                    "end;" +

                    // the lock cannot be acquired
                    // check if the thread is already in the queue
                    "local timeout = redis.call('zscore', KEYS[3], ARGV[2]);" + // 获取 当前线程的过期时间
                    "if timeout ~= false then " + //如果过期时间不为null 那么
                    // the real timeout is the timeout of the prior thread
                    // in the queue, but this is approximately correct, and
                    // avoids having to traverse the queue
                    "return timeout - tonumber(ARGV[3]) - tonumber(ARGV[4]);" +
                    "end;" +
                    // add the thread to the queue at the end, and set its timeout in the timeout set to the timeout of
                    // the prior thread in the queue (or the timeout of the lock if the queue is empty) plus the threadWaitTime
                    "local lastThreadId = redis.call('lindex', KEYS[2], -1);" +
                    "local ttl;" +
                    "if lastThreadId ~= false and lastThreadId ~= ARGV[2] then " + //如果队尾当前线程id不等于空(说明队列有元素) 并且 队尾线程id不等于当前请求加锁的线程id
                    "ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);" + // 那么等待时间是队尾元素的超时时间-当前时间
                    "else " +
                    "ttl = redis.call('pttl', KEYS[1]);" + //如果队列没有元素的话 ,或者是 队列中只有当前加锁元素 ,那么返回的等待时间就是 加锁hash key的过期时间
                    "end;" +
                    "local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);" +
                    "if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then " + //添加到zset集合中
                    "redis.call('rpush', KEYS[2], ARGV[2]);" +  //如果添加成功那么将线程id放入队尾
                    "end;" +
                    "return ttl;"
            , Long.class);


    private final static RedisScript<Long> TRY_UNLOCK = RedisScript.of(
            // remove stale threads
            "while true do "
                    + "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);"
                    + "if firstThreadId2 == false then "
                    + "break;"
                    + "end; "
                    + "local timeout = tonumber(redis.call('zscore', KEYS[3], firstThreadId2));"
                    + "if timeout <= tonumber(ARGV[4]) then "
                    + "redis.call('zrem', KEYS[3], firstThreadId2); "
                    + "redis.call('lpop', KEYS[2]); "
                    + "else "
                    + "break;"
                    + "end; "
                    + "end;"


                    +         "if (redis.call('exists', KEYS[1]) == 0) then " +
                        "local nextThreadId = redis.call('lindex', KEYS[2], 0); " +
                        "if nextThreadId ~= false then " +
                            "redis.call('PUBLISH', KEYS[4], ARGV[1] .. ':' .. nextThreadId); " +
                        "end; " +
                        "return 1; " +
                    "end;" +
                    "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                        "return nil;" +
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                    "if (counter > 0) then " +
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return 0; " +
                    "end; " +

                                       "redis.call('del', KEYS[1]); " +
                        "local nextThreadId = redis.call('lindex', KEYS[2], 0); " +

                        "if nextThreadId ~= false then " +
                            "redis.call('PUBLISH', KEYS[4], ARGV[1] .. ':' .. nextThreadId); " +
                        "end; " +
                    "return 1; ",
            Long.class
    );


    /**
     *
     */
    private final static String LOCK_QUEUE_NAME = "redis_lock_queue:";
    private final static String LOCK_TIMEOUT_NAME = "redis_lock_timeout:";

    public RedisFairLock(String name, StringRedisTemplate stringRedisTemplate, MessageReceiver messageReceiver) {
        this(name, stringRedisTemplate, messageReceiver, 300000L); //默认的等待时间 默认是300s即5分钟
    }

    public RedisFairLock(String name, StringRedisTemplate stringRedisTemplate, MessageReceiver messageReceiver, long threadWaitTime) {
        super(Thread.currentThread().getId(), name, stringRedisTemplate, messageReceiver);
        this.threadWaitTime = threadWaitTime;
    }

    private String getQueueName() {
        return LOCK_QUEUE_NAME + getBusinessName();
    }

    private String getTimeoutSetName
            () {
        return LOCK_TIMEOUT_NAME + getBusinessName();
    }


    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     */
    @Override
    public void lock() {
        //设置redi
        //1.1 成功
        // 1.1.1 设置看门狗
        //1.2 失败
        //1.2.1 订阅
        //1.2.2 锁重试
        //1.2.3 成功则设置看门狗,失败继续重试
        Long ttl = tryAc(-1, null); //传入过i时间为-1,时间单位为null
        log.info(getHashKeyName() + " ttl:" + ttl);
        if (ttl == null) { //如果返回值为null 说明加锁成功
            return;
        }
        //能够执行到这里说明第一次加锁不成功
        //订阅消息
        log.info(getHashKeyName() + " 首次尝试获取锁失败");
        while (true) {
            //再次尝试获取锁
            ttl = tryAc(-1, null); //传入过i时间为-1,时间单位为null
            if (ttl == null) { //如果返回值为null 说明加锁成功
                break;
            }
            try {
                getMessageReceiver().get(getBusinessName() + ":" + getHashKeyName()).acquire(); //阻塞获取信号量  通过 业务key 与线程id组合的形式
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Long tryAc(long lessTime, TimeUnit timeUnit) {
        CompletableFuture<Long> ttlTask;
        if (lessTime == -1) {
            ttlTask = supplyAsync(() -> lockRequestInnerAsync(TRY_LOCK, DEFAULT_LESS_TIME, getHashKeyName()));//尝试异步获取锁
        } else {
            ttlTask = supplyAsync(() -> lockRequestInnerAsync(TRY_LOCK, timeUnit.toMillis(lessTime), getHashKeyName()));//尝试异步获取锁,自定义过i时间
        }
        ttlTask.thenApply(ttl -> { //异步请求redis 完成后 判断是否需要 启动看门狗
            if (ttl == null) { //获取锁成功
                log.info(getHashKeyName() + " 获取锁成功");
                if (lessTime > 0) { //如果有手动设置过期时间,那么不启动看门狗

                } else {
                    //启动看门狗
                    watchDogSchedule();
                }
            }
            return ttl;
        });
        return get(ttlTask);
    }

    /**
     * description 异步尝试加锁执行函数
     *
     * @author: woldier
     * @date: 2023/7/29 上午8:35
     */
    private Long lockRequestInnerAsync(RedisScript<Long> redisScript, Long lessTime, String hashKeyName) {
        long wait = threadWaitTime;
        long currentTime = System.currentTimeMillis();
        return getStringRedisTemplate().execute(redisScript,
                Arrays.asList(getName(), getQueueName(), getTimeoutSetName()),
                String.valueOf(lessTime), //过期时间
                hashKeyName, // UUID:threadId
                String.valueOf(wait),//wait time
                String.valueOf(currentTime) //current time
        );
    }

    private String getBusinessName() {
        return getName().substring(5);
    }

    /**
     * Releases the lock.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation will usually impose
     * restrictions on which thread can release a lock (typically only the
     * holder of the lock can release it) and may throw
     * an (unchecked) exception if the restriction is violated.
     * Any restrictions and the exception
     * type must be documented by that {@code Lock} implementation.
     */
    @Override
    public void unlock() {
        tryRelease(TRY_UNLOCK);
    }

    @Override
    protected void tryRelease(RedisScript<Long> redisScript) {
        try {
            supplyAsync(() ->
                    getStringRedisTemplate().execute(redisScript,
                            Arrays.asList(getName(), getQueueName(), getTimeoutSetName(), "publish_channel_lock"),
                            getBusinessName(),//ARGV[1]发布的消息内容
                            String.valueOf(DEFAULT_LESS_TIME),//过期时间
                            getHashKeyName(), //ARGV[3]锁名称
                            String.valueOf(System.currentTimeMillis()) //当前时间
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
                } else if (res == 1L) {
                    //重入次数变为0,删除map来停止看门狗
                    log.info(getHashKeyName() + " 重入次数为0");
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
