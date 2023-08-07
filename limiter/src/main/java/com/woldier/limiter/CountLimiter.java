package com.woldier.limiter;

/**
 * description 计数器法
 *
 * @author: woldier wong
 * @date: 2023/8/7$ 21:23$
 */
public class CountLimiter extends AbstractLimiter{
    public CountLimiter(int requestCount, int limitNum, long interval) {
        super(limitNum, interval);
    }

    @Override
    public synchronized boolean limit() {
        long now = System.currentTimeMillis();
        if(now<timeStamp+interval){ //没有超过当前时间间隔
            if(requestCount+1> limitNum){
                return true;//被限流了
            }
            requestCount++;
            return false;
        }else { //过期了,开启一个新的计时窗口
            timeStamp = now;
            //重置计数器
            requestCount =1 ;
            return false;
        }

    }
}
