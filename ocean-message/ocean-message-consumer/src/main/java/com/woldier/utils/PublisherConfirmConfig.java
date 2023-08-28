package com.woldier.utils;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublisherConfirmConfig {
    public static final String EXCHANGE_NAME = "boot-advance-dle";
    public static final String DEAD_EXCHANGE_NAME = "boot-advance-dle-dead";
    public static final String QUEUE_NAME = "boot-advance-dle-topic-queue";
    public static final String DEAD_QUEUE_NAME = "boot-advance-dle-dead-topic-queue";
    private static final String ROUTING_KEY = "woldier.#";
    private static final String DEAD_ROUTING_KEY = "woldier.dead.#";
    //ttl时间(毫秒)
    private static final int TTL_EXPIRATION = 5000;
    /*设置为更大*/
    //private static final int TTL_EXPIRATION = 55000;

    /**
     * 交换机
     */
    @Bean("bootExchange")
    public Exchange bootExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    @Bean("bootExchangeDead")
    public Exchange bootExchangeDead() {
        return ExchangeBuilder.topicExchange(DEAD_EXCHANGE_NAME).durable(true).build();
    }

    /**
     * 队列 ,
     */
    @Bean("bootQueue") //Bean没有指定名字 则默认为方法名
    public Queue bootQueue() {
        /**
         * 设置过期参数
         * 绑定死信交换机
         * 绑定死信routingkey(在死信消息发布过程中 我们可以看作是一个publisher)
         */
        return QueueBuilder.durable(QUEUE_NAME).

                withArgument("x-message-ttl", TTL_EXPIRATION).
                /*
                How many (ready) messages a queue can contain before it starts to drop them from its head.(Sets the "x-max-length" argument.)
                设置queue最大容量,超过则会drop队顶数据*/
                        //withArgument("x-max-length",10).
                /*死信交换机设置*/
                        withArgument("x-dead-letter-exchange", DEAD_EXCHANGE_NAME).
                /*死信路由设置*/
                        withArgument("x-dead-letter-routing-key", "woldier.dead.111")
                .build();
    }

    /*死信队列*/
    @Bean("bootQueueDead") //Bean没有指定名字 则默认为方法名
    public Queue bootQueueDead() {
        return QueueBuilder.durable(DEAD_QUEUE_NAME).build();
    }

    /**
     * 绑定
     */

    @Bean
    public Binding bindQueue2Exchange(
            @Qualifier("bootExchange") Exchange bootExchange, //若容器中有多个exchange可以使用变量名代表要注入哪一个
            @Qualifier("bootQueue") Queue queue //也可以使用@Qualifier指定
    ) {
        /*绑定并设置路由信息*/
        return BindingBuilder.bind(queue).to(bootExchange).with(ROUTING_KEY).noargs();
    }

    /*死信交换机与queue绑定*/
    @Bean
    public Binding bindQueue2ExchangeDead(
            @Qualifier("bootExchangeDead") Exchange bootExchange, //若容器中有多个exchange可以使用变量名代表要注入哪一个
            @Qualifier("bootQueueDead") Queue queue //也可以使用@Qualifier指定
    ) {
        /*绑定并设置路由信息*/
        return BindingBuilder.bind(queue).to(bootExchange).with(DEAD_ROUTING_KEY).noargs();
    }
}