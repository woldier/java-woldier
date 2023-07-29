package com.wolder.lock;

import com.wolder.config.MessageReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
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
}
