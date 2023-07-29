package com.wolder.lock;

import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * description 抽象类
 *
 * @author: woldier
 * @date: 2023/7/28 上午11:16
 */
@Getter
public abstract class AbstractRedisLock implements RLock {
    protected String  id;  //UUID
    protected final Long threadId; //线程id
    protected final String name;  //业务名称
    protected final StringRedisTemplate stringRedisTemplate;
    protected final static  int DEFAULT_LESS_TIME = 30_000;
    protected final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MICROSECONDS;
    public AbstractRedisLock(String id, Long threadId, String name, StringRedisTemplate stringRedisTemplate) {
        this.id = id;
        this.threadId = threadId;
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * description 返回redis中的key
     *
     * @return 返回redis中的key
     * @author: woldier
     * @date: 2023/7/28 上午11:31
     */
    public String getHashKeyName() {
        return id + ":" + String.valueOf(threadId);
    }
    /**
    *
    * description 尝试加锁
    *
    * @author: woldier
    * @date: 2023/7/29 上午8:24
    */
    protected Long lockRequest(RedisScript<Long> redisScript, long lessTime, TimeUnit timeUnit, String hashKeyName) {
        return getStringRedisTemplate().execute(redisScript, Collections.singletonList(getName()), String.valueOf(timeUnit.toMillis(lessTime)), hashKeyName);
    }
}
