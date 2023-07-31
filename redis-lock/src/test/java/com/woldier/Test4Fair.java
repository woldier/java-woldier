package com.woldier;

import com.wolder.Main;
import com.wolder.lock.RLock;
import com.wolder.lock.RedisLockUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {Main.class})
public class Test4Fair {
    @Autowired
    RedisLockUtils redisLockUtils;
    @Test
    public void test(){

        RLock lock = redisLockUtils.createFairLock("woldier");
            lock.lock();
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
}
