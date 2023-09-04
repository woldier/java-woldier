package com.woldier.utils;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 消费者的配置类
 */
@Configuration
public class ConsumerConfig {
    /**
     * 监听器配置
     * @param message
     */
    @RabbitListener(queues = "boot-advance-dle-dead-topic-queue") // queues参数设置队列名称
    public void listen(Message message , Channel channel) throws IOException {

        try {
            /*事务操作*/
            System.out.println("message = " + message + ", channel = " + channel);
//            int i = 1/0;
            /*成功ack*/
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }
        catch (Exception e){
            /*添加错误ack 参数分别为 tag , multiple , requeue */
            /**
             * 这里设置requeue为false 此消息就会被加入到死信队列
             */
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }
}