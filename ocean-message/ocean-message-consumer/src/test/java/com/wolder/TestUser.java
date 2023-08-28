package com.wolder;

import com.woldier.Main;


import com.woldier.mapper.UsersMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * description TODO
 *
 * @author: woldier wong
 * @date: 2023/8/27$ 16:34$
 */
@SpringBootTest(classes = Main.class)

public class TestUser {
    @Autowired
    UsersMapper usersMappers;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void test(){
        usersMappers.getUsers("user01",0,10000);
    }


    @Test
    public void test2(){
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
        /*正确的 */
        rabbitTemplate.convertAndSend("boot-advance-dle","woldier.1","confirm advance",new CorrelationData());
    }
}
