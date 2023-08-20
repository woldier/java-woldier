package com.woldier;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * description 定时器实现类
 *
 * @author: woldier wong
 * @date: 2023/8/20$ 12:39$
 */
@Slf4j
public class WheelTimer implements Timer{

    private static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS ;  //默认时间单位是毫秒

    private static int DURATION = 100;
    private static int WHEEL_SIZE = 60 * 1000 / DURATION; //时间单位是   100ms , 总的时间是60s



    private volatile long startTime = System.currentTimeMillis();
    /**
     * 用于标记当再时间轮的第一个
     */
    private volatile int cursor = 0;
    private Thread workerThread ;


    /***
    *
    * description TODO
    *
    * @param null
    * @return
    * @author: woldier wong
    * @date: 2023/8/20 13:23
    */

    /**
    * 时间轮
    */
//    private LinkedBlockingQueue[] wheel = new LinkedBlockingQueue[WHEEL_SIZE];
        private ArrayList<LinkedBlockingQueue<wheelTimeout>> wheel = new ArrayList<>(WHEEL_SIZE + WHEEL_SIZE>>>1);
    /**
    *
    * description 线程池
    *
    * @author: woldier wong
    * @date: 2023/8/20 13:00
    */
    private  final ExecutorService executors = Executors.newScheduledThreadPool(10);

    {
        log.debug("初始化时间轮");
        for(int i=0;i<WHEEL_SIZE;i++){
            wheel.add(new LinkedBlockingQueue<>());
        }

        workerThread = new Thread(worker(),"wheelTimer-worker");
        workerThread.start();
    }

    /**
    *
    * description 新建一个定时任务
    *
    * @param task 任务
     * @param timeout 超时时间
     * @param timeUnit  时间单位
    * @return com.woldier.Timeout
    * @author: woldier wong
    * @date: 2023/8/20 12:39
    */
    @Override
    public Timeout newTimeout(TimerTask task, long t, TimeUnit timeUnit) {
        long current = System.currentTimeMillis();
        long timeout = timeUnit.toMillis(t);
        int cur = cursor + (int) (timeout/DURATION); //得到游标
        wheelTimeout timeOut = new wheelTimeout(current + t, task, this); //创建一个新的
        wheel.get(cur).offer(timeOut); //插入的队列中
        return timeOut;
    }

    @Override
    public Set<Timeout> stop() {
        return null;
    }


    private Runnable worker(){
        return ()->{
            log.debug("开启定时线程");
            long fixedTime = DURATION ;
            for(;;){
                log.debug("开启处理");
                long s = System.currentTimeMillis();
                //然后将当前指针指向的
                int cur = cursor;
                //拿出所有的任务,做任务.
                doTimerTask(cur);
                //休息100ms ,然后
                sleep(fixedTime);
                cursor = (cursor + 1 ) % WHEEL_SIZE;
                startTime = startTime + DURATION; //加100ms
                fixedTime = DURATION - (System.currentTimeMillis() - s -DURATION);  //有可能延迟时间除了问题,因此需要重新计算休息的时间
            }
        };
    }

    private void doTimerTask(int cur) {
        LinkedBlockingQueue<wheelTimeout> q = wheel.get(cur);
        while(!q.isEmpty()){
            wheelTimeout polled = q.poll();
            executors.submit(()->{
                try {
                    polled.getTask().run(polled);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static void sleep(long time) {
        try {
            DEFAULT_TIME_UNIT.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
    *
    * description 内部类,用于task保存信息
    *
    * @author: woldier wong
    * @date: 2023/8/20 12:58
    */
    class wheelTimeout implements Timeout{

        /**
         *
         */
        private AtomicBoolean canceled = new AtomicBoolean(false) ;



        /**
         * 过期时间
         */
        private final long timeout;
        /**
        * 定时任务
        */
        private final TimerTask task;

        /**
         * 定时器
         */
        private final Timer timer;

        public wheelTimeout(long timeout, TimerTask task, Timer timer) {
            this.timeout = timeout;
            this.task = task;
            this.timer = timer;
        }

        /***
         *
         * description 获取Timer
         *
         *
         * @return com.woldier.Timer
         * @author: woldier wong
         * @date: 2023/8/20 12:57
         */
        @Override
        public Timer getTimer() {
            return timer;
        }

        /**
         * description 获取任务
         *
         * @return com.woldier.TimerTask
         * @author: woldier wong
         * @date: 2023/8/20 12:57
         */
        @Override
        public TimerTask getTask() {
            return task;
        }

        /**
         * description 检查是否过期
         *
         * @return boolean
         * @author: woldier wong
         * @date: 2023/8/20 12:57
         */
        @Override
        public boolean isExpired() {
            long current = System.currentTimeMillis();
            return current - timeout > 0 ;
        }

        /**
         * description 检查是否退出
         *
         * @return boolean
         * @author: woldier wong
         * @date: 2023/8/20 12:58
         */
        @Override
        public boolean isCancelled() {
            return canceled.get();
        }

        /**
         * description 退出
         *
         * @return void
         * @author: woldier wong
         * @date: 2023/8/20 12:58
         */
        @Override
        public void cancel() {
            if(canceled.get()){
                throw new RuntimeException("task is already canceled");
            }
            canceled.compareAndSet(false,true);
        }
    }




}
