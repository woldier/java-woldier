package com.wolder.lock;

import com.wolder.config.MessageReceiver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
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
            //"-- 删除超过等待时间的key\n" +
            "                    while true do \n" +
                    "                        local firstThreadId2 = redis.call('lindex', KEYS[2], 0);  --获取queue中头节点其值为 UUID:threadId\n" +
                    "                        if firstThreadId2 == false then --如果为null 说明当前没有等待加锁线程,直接跳出循环\n" +
                    "                            break;\n" +
                    "                        end;\n" +
                    "-- 获取timeout zset集合中对应元素的值(过期时间)\n" +
                    "                        local timeout = tonumber(redis.call('zscore', KEYS[3], firstThreadId2));  \n" +
                    "    -- 如果timeout时间小于当前时间(ARGV[4) 那么就将对应的线程 UUID:threadId 从队列和过期时间zset中移除\n" +
                    "                        if timeout <= tonumber(ARGV[4]) then\n" +
                    "                            -- remove the item from the queue and timeout set\n" +
                    "                            -- NOTE we do not alter any other timeout\n" +
                    "                            redis.call('zrem', KEYS[3], firstThreadId2);\n" +
                    "                            redis.call('lpop', KEYS[2]);\n" +
                    "                        else \n" +
                    "                            break;\n" +
                    "                        end;\n" +
                    "                    end;\n" +
                    "                    -- check if the lock can be acquired now\t\n" +
                    "                    if (redis.call('exists', KEYS[1]) == 0) -- 是否存在对应的业务key\n" +
                    "                        and ((redis.call('exists', KEYS[2]) == 0) -- 当前业务key的等待队列不存在\n" +
                    "                            or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then  -- 当前业务key等待队列的队头元素是要加锁的线程表示\n" +
                    "                       -- remove this thread from the queue and timeout set\n" +
                    "                       redis.call('lpop', KEYS[2]); -- 从队列中移除到当前队头元素\n" +
                    "                       redis.call('zrem', KEYS[3], ARGV[2]); -- 从超时时间zset中移除对应的线程\n" +

                    "                        -- decrease timeouts for all waiting in the queue\n" +
                    "                        local keys = redis.call('zrange', KEYS[3], 0, -1); -- 就获取zset集合中的所有元素，赋值给keys\n" +
                    //      -- 而zscore的设置是: 上一个锁的score+waitTime+currentTime\n
                    "     -- 让整个set集合中的元素都减掉waitTime \n" +
                    "                        for i = 1, #keys, 1 do  -- 有点不知道在干嘛\n" +
                    "                            redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);\n" +
                    "                        end;\n" +

                    "                        -- acquire the lock and set the TTL for the lease\n" +
                    "                        redis.call('hset', KEYS[1], ARGV[2], 1);  --上锁\n" +
                    "                        redis.call('pexpire', KEYS[1], ARGV[1]);  --刷新过期时间\n" +
                    "                        return nil;\n" +
                    "                    end;\n" +
                    "\n" +
                    "                    -- check if the lock is already held, and this is a re-entry\n" +
                    "                    if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then  -- 判断是重入\n" +
                    "                        redis.call('hincrby', KEYS[1], ARGV[2],1);\n" +
                    "                        redis.call('pexpire', KEYS[1], ARGV[1]);\n" +
                    "                        return nil;\n" +
                    "                    end;\n" +
                    "\n" +
                    "                    -- the lock cannot be acquired\n" +
                    "                    -- check if the thread is already in the queue\n" +
                    "                    local timeout = redis.call('zscore', KEYS[3], ARGV[2]); -- 加锁失败 查看是否已经在队列中\n" +
                    "                    if timeout ~= false then\n" +
                    "                        -- the real timeout is the timeout of the prior thread\n" +
                    "                        -- in the queue, but this is approximately correct, and\n" +
                    "                        -- avoids having to traverse the queue\n" +
                    "                        return timeout - tonumber(ARGV[3]) - tonumber(ARGV[4]);\n" +
                    "                    end;\n" +
                    "\n" +
                    "                    -- add the thread to the queue at the end, and set its timeout in the timeout set to the timeout of\n" +
                    "                    -- the prior thread in the queue (or the timeout of the lock if the queue is empty) plus the\n" +
                    "                    -- threadWaitTime\n" +
                    "                    local lastThreadId = redis.call('lindex', KEYS[2], -1);\n" +
                    "                    local ttl;\n" +
                    "                    if lastThreadId ~= false and lastThreadId ~= ARGV[2] then\n" +
                    "                        ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);\" +\n" +
                    "                    else \n" +
                    "                        ttl = redis.call('pttl', KEYS[1]);\n" +
                    "                    end;\n" +
                    "                    local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);\n" +
                    "                    if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then\n" +
                    "                        redis.call('rpush', KEYS[2], ARGV[2]); \n" +
                    "                    end;\n" +
                    "                    return ttl;"
            , Long.class);
    /**
     *
     */
    private final static String LOCK_QUEUE_NAME = "redis_lock_queue:";
    private final static String LOCK_TIMEOUT_NAME = "redis_lock_timeout:";

    public RedisFairLock( String name, StringRedisTemplate stringRedisTemplate, MessageReceiver messageReceiver) {
        this(name,stringRedisTemplate,messageReceiver,60000*5); //默认的等待时间
    }

    public RedisFairLock( String name, StringRedisTemplate stringRedisTemplate, MessageReceiver messageReceiver,long threadWaitTime) {
        super(Thread.currentThread().getId(), name, stringRedisTemplate, messageReceiver);
        this.threadWaitTime = threadWaitTime;
    }

    private String getQueueName() {
        return LOCK_QUEUE_NAME + getName();
    }

    private String getTimeoutSetName
            () {
        return LOCK_TIMEOUT_NAME + getName();
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
                Arrays.asList(getName(), getQueueName(),getTimeoutSetName()),
                String.valueOf(lessTime), //过期时间
                hashKeyName, // UUID:threadId
                String.valueOf(currentTime), //current time
                String.valueOf(wait)  //wait time

        );
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

    }
}
