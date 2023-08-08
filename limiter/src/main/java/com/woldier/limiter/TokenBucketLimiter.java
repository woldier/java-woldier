package com.woldier.limiter;

import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
*
* description 令牌桶
*
* @author: woldier wong
* @date: 2023/8/8 17:00
*/
@Slf4j
public class TokenBucketLimiter extends AbstractLimiter{
    private final BlockingDeque<Integer> deque ;
    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();  //定时任务
    public TokenBucketLimiter(int limitNum, long interval) {
        super(limitNum, interval);
        this.deque = new LinkedBlockingDeque<>(limitNum);
        renew(interval);
    }
    private void renew(long interval) {
        hashedWheelTimer.newTimeout(timeout -> {
            token();
            this.renew(interval);
        }, interval, TimeUnit.MILLISECONDS);
    }

    private void token(){
        try {
            deque.add(1);
        } catch (IllegalStateException e) {
            log.debug("满了");
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
    public boolean limit() throws InterruptedException {
        deque.take();
        return false;
    }
}
