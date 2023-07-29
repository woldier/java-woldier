package com.wolder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
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
//    @Bean
//    public MessageListenerAdapter listenerAdapter(MessageReceiver messageReceiver){
//        MessageListenerAdapter adapter = new MessageListenerAdapter(messageReceiver,"onMessage");
//        return adapter;
//    }
    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory,MessageReceiver messageReceiver){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        MessageListenerAdapter adapter = new MessageListenerAdapter(messageReceiver,"onMessage");
        container.addMessageListener(adapter, new PatternTopic("publish_channel_lock:"+"woldier"));
        return container;
    }

}
