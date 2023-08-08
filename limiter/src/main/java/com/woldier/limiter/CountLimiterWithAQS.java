package com.woldier.limiter;

import io.netty.util.HashedWheelTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
*
* description 用AQS实现阻塞的计数器限流阀
*
* @author: woldier wong
* @date: 2023/8/8 8:50
*/
public class CountLimiterWithAQS extends AbstractLimiter{
    private static final long serialVersionUID = 8028320027693858310L;
    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();  //定时任务
    public CountLimiterWithAQS(int limitNum, long interval) {
        super(limitNum, interval);
        sync = new Sync();
        renewTime(interval);
    }

    private void renewTime(long interval) {
        hashedWheelTimer.newTimeout(timeout->{
            sync.releaseShared(1);
            renewTime(interval);
        }, interval, TimeUnit.MILLISECONDS);
    }

    private final Sync sync;
    /**
     * description 限流方法
     *
     * @return boolean
     * @author: woldier wong
     * @date: 2023/8/7 20:59
     */
    @Override
    public boolean limit() {
        sync.acquireShared(1);
        return true;
    }

     class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -6928319362016345791L;

         public Sync() {
             setState(0);
         }


         /***
        *
        * description 尝试通过限流阀
        *
        * @param arg  尝试获取的数量
        * @return int
        * @author: woldier wong
        * @date: 2023/8/8 8:54
        */
        @Override
        protected int tryAcquireShared(int unUsed) {
           for (;;){
               long now = System.currentTimeMillis();
               if(now<timeStamp+interval){ //没有超过时间间隔
                   int count = getState();
                   int remain = limitNum-count;
                   if(remain-1<0 || compareAndSetState(count,count+1)){
                       return remain-1;//被限流了,返回0继续等待
                   }
               }
//               else {//过期了,开启一个新的窗口
//                   synchronized (this){
//                       if(now>=timeStamp+interval){
//                           timeStamp = now;
//                           setState(1);
//                           return 1;
//                       }
//                   }
//               }
           }

        }
         @Override
         protected boolean tryReleaseShared(int arg) {
            for(;;){
                if(compareAndSetState(getState(),0)){
                    timeStamp = System.currentTimeMillis();
                    return true;
                }
            }
         }
     }
}
