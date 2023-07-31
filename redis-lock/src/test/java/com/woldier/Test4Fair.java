package com.woldier;

import com.wolder.Main;
import com.wolder.lock.RLock;
import com.wolder.lock.RedisLockUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {Main.class})
public class Test4Fair {
    @Autowired
    RedisLockUtils redisLockUtils;
    @Test
    public void test() throws InterruptedException {

        new Thread(()->{
            RLock lock = redisLockUtils.createFairLock("woldier-fair");
            lock.lock();
        }).start();
        new Thread(()->{
            RLock lock = redisLockUtils.createFairLock("woldier-fair");
            lock.lock();
        }).start();

        TimeUnit.SECONDS.sleep(40);
    }
}
