package com.wolder.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
*
* description redis消息监听并配置类
*        
* @author: woldier 
* @date: 2023/7/28 上午9:03
*/
@Configuration
public class RedisConfig {

    @Bean
    public MessageListenerAdapter listenerAdapter(MessageReceiver messageReceiver){
        MessageListenerAdapter adapter = new MessageListenerAdapter(messageReceiver,"onMessage");
        return adapter;
    }
    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory,MessageListenerAdapter adapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(adapter, new PatternTopic("publish_channel_lock"));
        return container;
    }

}
