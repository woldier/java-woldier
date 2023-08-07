package com.woldier.limiter;

/**
 * description 抽象类,定义了一些控制的常量
 *
 * @author: woldier wong
 * @date: 2023/8/7$ 20:59$
 */
public abstract class AbstractLimiter implements Limiter {
    protected long timeStamp = System.currentTimeMillis();
    protected  int  requestCount; //请求次数
    protected final  int limitNum ;//最大限流数

    protected final long interval ;//时间窗口市场,单位ms

    public AbstractLimiter( int limitNum, long interval) {

        this.limitNum = limitNum;
        this.interval = interval;
    }

}
