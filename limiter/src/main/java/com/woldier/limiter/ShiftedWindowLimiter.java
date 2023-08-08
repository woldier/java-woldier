package com.woldier.limiter;

import java.util.LinkedList;
import java.util.List;

/**
*
* description 滑动窗口
*
* @author: woldier wong
* @date: 2023/8/8 11:33
*/
public class ShiftedWindowLimiter extends AbstractLimiter{
    private  final  static int MAX_WINDOWS = 0x10; //定义最多的窗口数目
    private final int[] countWindow = new int[MAX_WINDOWS]; //时窗存放
    private int head; //头指针
//    private
    public ShiftedWindowLimiter(int limitNum, long interval) {
         super(limitNum, interval);
    }

    /**
     * description 限流方法
     *
     * @return boolean
     * @author: woldier wong
     * @date: 2023/8/7 20:59
     */
    @Override
    public  boolean  limit() {
        synchronized (this){
            long now = System.currentTimeMillis(); //得到当前时间
            if(now< timeStamp+ interval) {//没有超过
                //检查是否超过了最大限制
                if(requestCount<limitNum) {
                    // 计算当前应该存放在第几个格子
                    int index = (int) ((now - timeStamp) * MAX_WINDOWS / interval);
                    countWindow[toIndex( index)] =  countWindow[ toIndex( index) ] +1; //计数加1
                    requestCount ++;
                    return false;//满了,拒绝
                }
                return true;
            }else {//超过了
                int step = (int) ((now - timeStamp - interval) * MAX_WINDOWS / interval  +1);
                for (int i=0;i<step;i++){
                    requestCount -= countWindow[ toIndex( i) ]; //移除总计数
                    countWindow[toIndex(i)] = 0; // 清零
                }
                head = (head +step)  % MAX_WINDOWS;
                timeStamp = timeStamp + interval + step * (interval / MAX_WINDOWS);
                return false;
            }
        }

    }

    private int toIndex(int index) {
        return (index + head) % MAX_WINDOWS;
    }
}
