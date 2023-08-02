package com.woldier;

import com.wolder.Main;
import com.wolder.lock.RLock;
import com.wolder.lock.RedisLockUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {Main.class})
public class Test4Fair {
    @Autowired
    RedisLockUtils redisLockUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;  //redis模板
    @Test
    public void test() throws InterruptedException {

        Thread thread1 = getThread();
        thread1.start();
        TimeUnit.SECONDS.sleep(2);

        Thread thread2 = getThread();
        thread2.start();
        TimeUnit.SECONDS.sleep(2);

        Thread thread3 = getThread();
        thread3.start();
        TimeUnit.SECONDS.sleep(2);

        Thread thread4 = getThread();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

    }

    private Thread getThread() {
        return new Thread(() -> {
            RLock lock = redisLockUtils.createFairLock("woldier-fair");
            lock.lock();
            try {
                method();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lock.unlock();
        });
    }

    public void method() throws InterruptedException {
        RLock lock = redisLockUtils.createFairLock("woldier-fair");
        lock.lock();
        TimeUnit.SECONDS.sleep(15);
        lock.unlock();
    }


    @Test
    public void testRedis(){
        //"redis.call('PUBLISH', KEYS[4], ARGV[1] .. ':' .. nextThreadId); "

        System.out.println(stringRedisTemplate.execute(RedisScript.of("local nextThreadId = redis.call('lindex', KEYS[2], 0); return nextThreadId;", String.class),
                Arrays.asList("", "", "", "publish_channel_lock"),
                "woldier",//ARGV[1]发布的消息内容
                String.valueOf(1),//过期时间
                "", //ARGV[3]锁名称
                "" //当前时间
        ));
    }
}
