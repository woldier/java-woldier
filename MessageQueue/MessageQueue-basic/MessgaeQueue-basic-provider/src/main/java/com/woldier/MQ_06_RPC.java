package com.woldier;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MQ_06_RPC {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        // * 1.创建工厂
        ConnectionFactory factory = new ConnectionFactory();
        // * 2.设置连接参数
        factory.setHost("tencent.woldier.top");//设置ip
        factory.setPort(5672); //设置端口,管理页面中可以查看
        factory.setVirtualHost("/woldier");//设置虚拟机
        factory.setUsername("admin");
        factory.setPassword("123456");
        // * 3.通过工厂对象创建连接
        Connection connection = factory.newConnection();
        // * 4.设置channel
        Channel channel = connection.createChannel();
        // * 5.设置queen
//        void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments) throws IOException;
        //channel.queueDeclare("rpc-queues",true,false,false,null);
        // * 6.发送消息
        final String corrId = UUID
                .randomUUID().toString();  //得到一个uuid作为correlationId

        String replyQueueName = channel.queueDeclare().getQueue();  //随机生成一个用于返回调用返回的queue
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        String msg = "11";

        channel.basicPublish("","rpc-queues",props,new String(msg).getBytes());  //发起请求

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });
        String result = response.get();  //阻塞等待返回
        System.out.println("result = " + result);
        channel.basicCancel(ctag); //告知mq处理ok
        /*
        释放资源
         */
        channel.close();
        connection.close();

    }
}
