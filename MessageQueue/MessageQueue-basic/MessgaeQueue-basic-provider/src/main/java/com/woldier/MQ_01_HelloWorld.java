package com.woldier;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MQ_01_HelloWorld {
    /**
     * 步骤如下
     * 1.创建连接工厂
     * 2.设置连接工厂连接参数
     * 3.通过工厂对象创建连接
     * 4.创建channel
     * 5.channel创建queen
     * 6.发送消息
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
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

        /**
         像queueDeclare(String, boolean, boolean, boolean, Map)，
         但是设置nowait标志为true并且不返回结果(因为没有来自服务器的响应)。
         参数:
         队列——队列的名称 ,有该名称的队列则不新建.
         耐久——如果我们声明了一个耐久队列，则为true(该队列在服务器重启后仍然有效)
         Exclusive—如果我们声明一个独占队列(仅限于此连接)，则为真。
         autoDelete -如果我们声明了一个自动删除队列，则为true(服务器将在不再使用时删除它) 参数-队列的其他属性(构造参数) 抛出: IOException -如果遇到错误
         */
//        void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments) throws IOException;
        channel.queueDeclare("hello_world",true,false,false,null);





        // * 6.发送消息
        /**
         Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception, which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm  is in effect.
         发布一条消息。发布到不存在的交换将导致通道级协议异常，从而关闭通道。如果资源驱动的告警生效，通道#basicPublish的调用将最终被阻塞
         Params:
         exchange – the exchange to publish the message to 将消息发布到的交换器 ,简单模式下为默认的给空字符就可以了
         routingKey – the routing key 路由键
         mandatory – true if the 'mandatory' flag is to be set 如果要设置' Mandatory '标志，则为true
         immediate – true if the 'immediate' flag is to be set. Note that the RabbitMQ server does not support this flag
         如果要设置' Immediate '标志，则为true。注意RabbitMQ服务器不支持这个标志
         props – other properties for the message - routing headers etc 消息的其他属性—路由标题等
         body – the message body 消息正问
         Throws:
         IOException – if an error is encountered
         See Also:
         AMQP.Basic.Publish, Resource-driven alarms
         */
        String msg = "hello rabbit -2";
        //void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props, byte[] body)throws IOException;
        channel.basicPublish("","hello_world",null,msg.getBytes());

        Thread.sleep(10000);

        /*
        释放资源
         */
        channel.close();
        connection.close();
    }
}