package com.woldier;

import com.wolder.Main;
import com.wolder.lock.RLock;
import com.wolder.lock.RedisLockUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {Main.class})
public class Test4Lock {
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;  //redis模板
    @Autowired
    private RedisLockUtils redisLockUtils;
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
        Long aLong = stringRedisTemplate.execute(script, Collections.singletonList("lock:test"), String.valueOf(100_000), "13123:asdasd");
        System.out.println(aLong);
//        Redisson.create().getLock("woldier").unlock();
    }


    @Test
    public void thenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> stage = CompletableFuture.supplyAsync(() -> "hello");
        CompletableFuture<String> future = stage.thenApply(s -> s + " world");
        String s = future.get();
        //String result = stage.thenApply(s -> s + " world").join();
        System.out.println(s);
    }

    @Test
    public void testLock() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            RLock lock = redisLockUtils.createLock("woldier");
            lock.lock();
            try {
                method(); //重入
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                TimeUnit.SECONDS.sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lock.unlock();
        });


        Thread thread2 =new Thread(()->{
            RLock lock = redisLockUtils.createLock("woldier");
            lock.lock();
            try {
                method(); //重入
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                TimeUnit.SECONDS.sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lock.unlock();
        });

        thread1.start();
        thread2.start();

        thread2.join();
        thread1.join();
        TimeUnit.SECONDS.sleep(15);
//        RLock lock = redisLockUtils.createLock("woldier");
//            lock.lock();
//            try {
//                method(); //重入
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                TimeUnit.SECONDS.sleep(25);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            lock.unlock();
//            Thread.sleep(55_000L);

    }

    private void method() throws InterruptedException {
        RLock lock = redisLockUtils.createLock("woldier");
        lock.lock();
        TimeUnit.SECONDS.sleep(25);
        lock.unlock();
    }

    @Test
    public void testTimer() throws InterruptedException {
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
        renew(hashedWheelTimer);
        TimeUnit.SECONDS.sleep(100);
    }

    private static void renew(HashedWheelTimer hashedWheelTimer) {
        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                // sout
                System.out.println("timeout = " + timeout);
                renew(hashedWheelTimer);
            }
        },10, TimeUnit.SECONDS);
    }
}
