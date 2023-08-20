package com.woldier;
/**
*
* description 对定时任务做封装
*
* @author: woldier wong
* @date: 2023/8/20 12:36
*/
public interface Timeout {
    /***
    *
    * description 获取Timer
    *
    *
    * @return com.woldier.Timer
    * @author: woldier wong
    * @date: 2023/8/20 12:57
    */
    Timer getTimer();

    /**
    *
    * description 获取任务
    *
    *
    * @return com.woldier.TimerTask
    * @author: woldier wong
    * @date: 2023/8/20 12:57
    */
    TimerTask getTask();

    /**
    *
    * description 检查是否过期
    *
    *
    * @return boolean
    * @author: woldier wong
    * @date: 2023/8/20 12:57
    */
    boolean isExpired();

    /**
    *
    * description 检查是否退出
    *
    *
    * @return boolean
    * @author: woldier wong
    * @date: 2023/8/20 12:58
    */
    boolean isCancelled();

    /**
    *
    * description 退出
    *
    *
    * @return void
    * @author: woldier wong
    * @date: 2023/8/20 12:58
    */
    void cancel();
}
