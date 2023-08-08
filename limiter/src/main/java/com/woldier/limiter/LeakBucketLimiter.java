package com.woldier.limiter;

import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
*
* description 漏桶算法
*
* @author: woldier wong
* @date: 2023/8/8 16:03
*/
@Slf4j
public   class  LeakBucketLimiter   extends AbstractLimiter{

    private final BlockingDeque<Integer> deque ;
    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();  //定时任务
    public LeakBucketLimiter(int limitNum, long interval) {
        super(limitNum, interval);
        this.deque = new LinkedBlockingDeque<>(limitNum);
        renew(interval);
    }

    private void renew(long interval) {
        hashedWheelTimer.newTimeout(timeout -> {
            leak();
            this.renew(interval);
        }, interval, TimeUnit.MILLISECONDS);
    }

    private void leak(){
        try {
            deque.removeFirst();
        } catch (NoSuchElementException e) {
            log.debug("空了");
        }
    }
    /**
     * description 限流方法
     *
     * @return boolean
     * @author: woldier wong
     * @date: 2023/8/7 20:59
     */
    @Override
    public synchronized boolean limit() throws InterruptedException {
        //先露出水
        deque.putLast(1);
        return false;
    }
}
