package com.wolder.config;

import org.redisson.api.listener.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements MessageListener {
    
    /**
     * Invokes on every message in topic
     *
     * @param channel of topic
     * @param msg     topic message
     */
    @Override
    public void onMessage(CharSequence channel, Object msg) {
        System.out.println("回调方法");
    }
}
