package com.woldier;

import com.wolder.Main;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = {Main.class})
public class Test4Lock {
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;  //redis模板
    String s = "if (redis.call('EXISTS',KEYS[1])==0) then  " +
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
            "return redis.call('PTTL',KEYS[1])";
    @Test
    public void test() {
//        RedisScript<Long> script = RedisScript.of("redis.call('SETEX' ,KEYS[1], 100, ARGV[1]); return redis.call('PTTL',KEYS[1]);", Long.class);
//        Long aLong = stringRedisTemplate.execute(script, Collections.singletonList("test"), "value");
        RedisScript<Long> script = RedisScript.of(s, Long.class);
        Long aLong = stringRedisTemplate.execute(script, Collections.singletonList("test"), String.valueOf(100_000),"13123:asdasd" );
        System.out.println(aLong);
        Redisson.create().getLock("woldier").unlock();
    }


    @Test
    public void thenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> stage = CompletableFuture.supplyAsync(() -> "hello");
        CompletableFuture<String> future = stage.thenApply(s -> s + " world");
        String s = future.get();
        //String result = stage.thenApply(s -> s + " world").join();
        System.out.println(s);
    }
}
