package com.woldier.utils;

import com.alibaba.fastjson.JSON;
import com.woldier.mapper.UsersMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * description TODO
 *
 * @author: woldier wong
 * @date: 2023/8/27$ 18:42$
 */
@Component
public class MessageSender{
    @Autowired
    UsersMapper usersMappers;
    @Autowired
    RabbitTemplate rabbitTemplate;

    public <T> boolean sendMsg(T msg, long timeout, TimeUnit timeUnit){
        /**
         * 设置一个confirm回调
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 相关数据
             * @param b 为true表示提交到交换机成功 为false表示到交换机失败
             * @param s 如果提交到交换机失败了的话 这里会给出错误信息
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {

                //回调得到
                System.out.println("correlationData = " + correlationData + ", b = " + b + ", s = " + s);
            }
        });

        /**
         * 这里我们模拟一个到exchange的异常 当前exchange的name 是不存在的
         */
        MessageProperties messageProperties = new MessageProperties();
        // 设置过期时间，单位：毫秒
        messageProperties.setExpiration(String.valueOf(timeUnit.toMillis(timeout)));
        Message message = new Message(JSON.toJSONString(msg).getBytes(StandardCharsets.UTF_8), messageProperties);
        /*正确的 */
        rabbitTemplate.convertAndSend("boot-advance-dle","woldier.1",message,new CorrelationData());
        return true;
    }


    public <T> boolean sendMsg(T msg, LocalDateTime dateTime){
        LocalDateTime now = LocalDateTime.now();
        Duration between = Duration.between(now, dateTime);
        long millis = between.toMillis();
        if(millis<0) throw new RuntimeException("当前时间不能超过预期时间");
        return sendMsg(msg,millis,TimeUnit.MILLISECONDS);
    }




    public void send(){

    }
}
