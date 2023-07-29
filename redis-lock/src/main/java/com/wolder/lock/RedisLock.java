package com.wolder.lock;

import cn.hutool.core.thread.AsyncUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * description 实现类
 *
 * @author: woldier
 * @date: 2023/7/28 上午11:23
 */
@Slf4j
public class RedisLock extends AbstractRedisLock {

    /**
     * 尝试获取锁方法
     * */
    private final static RedisScript<Long> TRY_LOCK = RedisScript.of("if (redis.call('EXISTS',KEYS[1])==0) then  " +
            "    redis.call('HSET',KEYS[1],ARGV[2],1); " +
            "    redis.call('PEXPIRE',KEYS[1],ARGV[1]); " +
            "    return nil;" +
            "end;" +
            "if(redis.call('HEXISTS',KEYS[1],ARGV[2])==1) then" +
            "    redis.call('HINCRBY',KEYS[1],ARGV[2],1); " +
            "    redis.call('PEXPIRE',KEYS[1],ARGV[1]); " +
            "    return nil;\n" +
            "end;\n" +
            "\n" +
            "return redis.call('PTTL',KEYS[1])", Long.class);

    /**
     * 尝试解锁
     */
    private final static RedisScript<Long> TRY_UNLOCK =
            RedisScript.of("if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                    "return nil;" + //返回null 说明是错误的解锁
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " + //得到重入次数减一后的结果
                    "if (counter > 0) then " + //如果当前重入次数任然大于零 那么不删除锁
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 0; " +  //没有删除锁返回0
                    "else " +
                    "redis.call('del', KEYS[1]); " +  //删除key
                   // "redis.call(SPUBLISH, KEYS[2], ARGV[1]); " + //发布订阅
                    "return 1; " +  //删除了锁返回1
                    "end; " +
                    "return nil;", Long.class);
    /**
     * description 构造函数
     *
     * @param name                锁的业务名称
     * @param stringRedisTemplate redis 连接
     * @return
     * @author: woldier
     * @date: 2023/7/29 上午8:03
     */
    public RedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        super(Thread.currentThread().getId(), name, stringRedisTemplate);
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
        //设置redis

        //1.1 成功
        // 1.1.1 设置看门狗
        //1.2 失败
        //1.2.1 订阅
        //1.2.2 锁重试
        //1.2.3 成功则设置看门狗,失败继续重试
        Long ttl = tryAc(-1, null); //传入过i时间为-1,时间单位为null
        if (ttl == null) { //如果返回值为null 说明加锁成功
            return;
        }
        //能够执行到这里说明第一次加锁不成功
        System.out.println(getHashKeyName()+" 首次尝试获取锁失败");
        throw new RuntimeException("加锁失败");

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
                System.out.println(getHashKeyName()+" 获取锁成功");
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
        return getStringRedisTemplate().execute(redisScript, Collections.singletonList(getName()), String.valueOf(lessTime), hashKeyName);
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




}
