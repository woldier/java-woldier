package com.woldier;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import sun.awt.Mutex;

/***
*
* description 测试mutex
*
* @author: woldier wong
* @date: 2023/8/3 10:14
*/
@Slf4j
public class Test2Mutex {
    @Test
    public void test(){
        Mutex mutex = new Mutex();
        mutex.lock();
        try {
           log.debug("do sth ....");
        }
        finally {
            mutex.unlock();
        }
    }
}
