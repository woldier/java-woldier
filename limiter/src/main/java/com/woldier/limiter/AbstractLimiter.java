package com.woldier.limiter;

/**
 * description 抽象类,定义了一些控制的常量
 *
 * @author: woldier wong
 * @date: 2023/8/7$ 20:59$
 */
public abstract class AbstractLimiter implements Limiter {
    private long timeStamp = System.currentTimeMillis(); 
    private final int  requestCount; //请求次数
    private final  int limitNum ;//最大限流数

    private final long interval ;//时间窗口市场,单位ms

    public AbstractLimiter(int requestCount, int limitNum, long interval) {
        this.requestCount = requestCount;
        this.limitNum = limitNum;
        this.interval = interval;
    }

}
