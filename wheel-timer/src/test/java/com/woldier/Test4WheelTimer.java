package com.woldier;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * description TODO
 *
 * @author: woldier wong
 * @date: 2023/8/20$ 14:06$
 */
@Slf4j
public class Test4WheelTimer {

    @Test
    public void test() throws InterruptedException {
        Timer wheelTimer = new WheelTimer();
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);


        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },5, TimeUnit.SECONDS);


        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);
        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },7, TimeUnit.SECONDS);



        wheelTimer.newTimeout(timeout->{
            log.debug("延迟");
        },9, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(10);
    }
}
