package com.wolder.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class MessageReceiver implements MessageListener<String> {
    private final static ConcurrentHashMap<String, Semaphore> SEMAPHORE_MAP = new ConcurrentHashMap<>();
    /**
    *
    * description 通过业务字段获取到对应的信号量
    *
    * @param name 业务名称
    * @return
    * @author: woldier
    * @date: 2023/7/29 下午3:58
    */
    public Semaphore get(String name){
        Semaphore semaphore = new Semaphore(1); //创建一个新的信号量
        Semaphore oldSemaphore = SEMAPHORE_MAP.putIfAbsent(name, semaphore); //如果key对应的不存在则放入
        return oldSemaphore!=null?oldSemaphore:semaphore;
    }

    /**
     * 为了让为获取锁的线程阻塞起来,这里使用信号量机制来控制线程阻塞
     * 当监听到对应的key释放消息,那么就释放信号量,那么获取锁的阻塞线程就可以继续运行
     * <p>
     *
     * /**
     * Invokes on every message in topic
     *
     * @param channel of topic
     * @param msg     topic message
     */
    @Override
    public void onMessage(CharSequence msg, String channel) {
        log.info("channel " + channel+" msg " + msg.toString());
        Semaphore semaphore = SEMAPHORE_MAP.getOrDefault(msg.toString(), null);
        if(semaphore==null){
            log.info("本机中不存在对应name锁的阻塞线程");
            return;
        }
        log.info("释放信号量");
        semaphore.release(); //释放信号量
    }
}
