package com.wolder.redis.lock;

import com.wolder.redis.config.MessageReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
*
* description 工具类,用于创建redis lock
*
* @author: woldier
* @date: 2023/7/29 上午8:02
*/
@Component
@RequiredArgsConstructor
public class RedisLockUtils {
    private final StringRedisTemplate stringRedisTemplate;
    private final MessageReceiver messageReceiver;

    /**
     * 创建一把redis_lock
     * @param name
     * @return
     */
    public RLock createLock(String name){
        return new RedisLock(name,stringRedisTemplate,messageReceiver);
    }

    /**
    *
    * description 创建一把公平锁
    *
    * @param name  业务key
    * @return com.wolder.lock.RLock
    * @author: woldier wong
    * @date: 2023/7/31 16:09
    */
    public RLock createFairLock(String name) {
        return new RedisFairLock(name,stringRedisTemplate,messageReceiver);
    }
}
