package com.woldier;

import com.wolder.Main;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

@SpringBootTest(classes = {Main.class})
public class Test4Lock {
    @Autowired(required = false)
    private  StringRedisTemplate stringRedisTemplate;  //redis模板
    @Test
    public void test(){
        RedisScript<Long> script = RedisScript.of("redis.call('SETEX' ,KEYS[1], 100, ARGV[1]); return redis.call('PTTL',KEYS[1]);", Long.class);
        Long aLong = stringRedisTemplate.execute(script, Collections.singletonList("test"),"value");
        System.out.println(aLong);




    }
}
