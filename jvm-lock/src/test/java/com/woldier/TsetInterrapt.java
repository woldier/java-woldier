package com.woldier;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TsetInterrapt {

    private static int tag = 1;
    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(() -> {
            boolean failed = true;
            try {
                boolean interrupted = false;
                log.debug("进入死循环");
                for (;;) {

                    if(tag == 0){
                        failed = false;
                        return;
                    }
                }
            } finally {
                if (failed)  // 最终会执行这个判断,如果是走的加锁成功那么failed一定是flase,其他情况就是true,这时候就要从queue中移除node
                    log.debug("失败标记为true");
            }
        }
        );

        thread.start();
        TimeUnit.SECONDS.sleep(2);
        log.debug("去打断");
        thread.interrupt();
        thread.join();
    }


    @Test
    public void test2() throws InterruptedException {
        Thread thread = new Thread(() -> {
            boolean failed = true;
            try {
                boolean interrupted = false;
                log.debug("进入死循环");
                for (;;) {
                    if(tag == 0){
                        failed = false;
                        return;
                    }
//                    throw new Error("异常跳出");
                    throw new RuntimeException("出错了");
                }
            } finally {
                if (failed)  // 最终会执行这个判断,如果是走的加锁成功那么failed一定是flase,其他情况就是true,这时候就要从queue中移除node
                    log.debug("失败标记为true");
            }
        }
        );
        thread.start();
        TimeUnit.SECONDS.sleep(2);
        thread.join();
    }
}
