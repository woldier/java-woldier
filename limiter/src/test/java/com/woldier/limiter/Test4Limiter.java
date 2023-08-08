package com.woldier.limiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
/**
 *
 * description TODO
 *
 * @author: woldier wong
 * @date: 2023/8/8 8:41
 */
@Slf4j
public class Test4Limiter {
    @Test
    public void test4CountLimiter() throws InterruptedException {
        Limiter limiter = new CountLimiter(1,2000);

        task(limiter);

    }

    private static void task(Limiter limiter) throws InterruptedException {
        Thread thread1 = new Thread(()->{
            try {
                while(limiter.limit());
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread2 = new Thread(()->{
            try {
                while(limiter.limit());;
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread3 = new Thread(()->{
            try {
                while(limiter.limit());;
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread4 = new Thread(()->{
            try {
                while(limiter.limit());;
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread2.start();
        thread1.start();
        thread3.start();
        thread4.start();
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }


    @Test
    public void test4CountLimiterWithAqs() throws InterruptedException {
        Limiter limiter = new CountLimiterWithAQS(2,2_000);

        task2(limiter);
    }

    private static void task2(Limiter limiter) throws InterruptedException {
        Thread thread1 = new Thread(()->{
            try {
               limiter.limit();
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread2 = new Thread(()->{
            try {
                limiter.limit();
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread3 = new Thread(()->{
            try {
                limiter.limit();
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread4 = new Thread(()->{
            try {
                limiter.limit();
                log.debug("doing...........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread2.start();
        thread1.start();
        thread3.start();
        thread4.start();
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }
}
