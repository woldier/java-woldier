package com.woldier.utils;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者的配置类
 */
@Configuration
public class ConsumerConfig {
    /**
     * 监听器配置
     * @param message
     */
    @RabbitListener(queues = "bootQueue") // queues参数设置队列名称
    public void Listen(Message message){
        System.out.println(message.toString());
    }
}