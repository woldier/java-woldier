package com.woldier.limiter;

/**
 * description 抽象接口
 *
 * @author: woldier wong
 * @date: 2023/8/7$ 20:14$
 */
public interface Limiter{
    /**
    *
    * description 限流方法
    *
    *
    * @return boolean
    * @author: woldier wong
    * @date: 2023/8/7 20:59
    */
    boolean limit() throws InterruptedException;
}
