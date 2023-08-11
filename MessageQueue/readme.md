> # 消息队列
>
> 以rabbitmq为例(site:黑马rabbitmq实战)



# 1.基础队列



**准备工作**

安装rabbitMq

本实验使用docker安装方式(带控制台插件).

安装过程可以参考https://juejin.cn/post/7198430801850105916

本文使用的virtual host 是`/woldier`

因此需要去创建,并且与某用户进行绑定.

![image-20230810093755453](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810093755453.png)

填入信息

![image-20230810094038376](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810094038376.png)

随后,点击刚刚创建virtual host可以进入到设置页面进行虚拟主机的配置

![image-20230810094259920](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810094259920.png)



## 1.1 hello world



- 生产者

```java
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
```

运行后我们就可以看到queue中有一条对应的消息

可以看到我们在发送的时候设置的`exchange`为空字符串,那么他到底是不是走的默认交换机呢?

![image-20230810094905244](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810094905244.png)

![image-20230810094839536](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810094839536.png)

通过web management可以发现,确实走的是默认的交换机

- 消费者

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * description hello world 消费者
 *
 * @author: woldier wong
 * @date: 2023/8/10 17:32
 */
public class MQ_01_HelloWorldConsumer {

    /**
     * 步骤如下
     * 1.创建连接工厂
     * 2.设置连接工厂连接参数
     * 3.通过工厂对象创建连接
     * 4.创建channel
     * 5.channel创建queen
     * 6.接收消息
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, TimeoutException {
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
        channel.queueDeclare("hello_world", true, false, false, null);


        //* 6.接收消息
        /**
         Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag and specified arguments. Provide access to basic.deliver, basic.cancel and shutdown signal callbacks (which is sufficient for most cases). See methods with a Consumer argument to have access to all the application callbacks.
         Params:
         queue – the name of the queue
         autoAck – true if the server should consider messages acknowledged once delivered; false if the server should expect explicit acknowledgements
         arguments – a set of arguments for the consume
         deliverCallback – callback when a message is delivered
         cancelCallback – callback when the consumer is cancelled
         shutdownSignalCallback – callback when the channel/connection is shut down
         Returns:
         the consumerTag generated by the server
         Throws:
         IOException – if an error is encountered
         Since:
         5.0
         See Also:
         AMQP.Basic.Consume, AMQP.Basic.ConsumeOk, basicAck, basicConsume(String, boolean, String, boolean, boolean, Map, Consumer)
         */
        //String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            /**
             * 实现回调方法 (原类中的回调方法为空实现)
             *
             * @param consumerTag
             * @param envelope
             * @param properties
             * @param body
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("consumerTag = " + consumerTag + ",\n envelope = " + envelope + ",\n properties = " + properties + ",\n body = " + new String(body));
            }
        };
        channel.basicConsume("hello_world", true, consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true) {}
    }
}


```



打印台打印

```shell
consumerTag = amq.ctag-tJiO7cV5jKCb6xlKTFmddg,
 envelope = Envelope(deliveryTag=1, redeliver=false, exchange=, routingKey=hello_world),
 properties = #contentHeader<basic>(content-type=null, content-encoding=null, headers=null, delivery-mode=null, priority=null, correlation-id=null, reply-to=null, expiration=null, message-id=null, timestamp=null, type=null, user-id=null, app-id=null, cluster-id=null),
 body = hello rabbit -2

```

去web management,可以看到消息被成功消费

![image-20230810173729921](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230810173729921.png)



## 1.2 work queue

![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Fe9739cdc23f652aed7384f7f3e6f5c30.png)

work queue 模式下,消息会分发给同一queue的消费者(类似于并行处理)

```java
package com.woldier;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * description 工作队列
 *
 * @author: woldier wong
 * @date: 2023/8/11$ 9:54$
 */
public class MQ_02_WorkQueue {
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
        channel.queueDeclare("work-queues",true,false,false,null);





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
        String msg = "hello rabbit ";
        //void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props, byte[] body)throws IOException;
        for (int i =0;i<10;i++){
            channel.basicPublish("","work-queues",null,new String(msg+i).getBytes());

        }



        /*
        释放资源
         */
        channel.close();
        connection.close();

    }
}


```

![image-20230811100816742](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F53bee211b697d4f0e74cfce9f7127487.png)

等待发送完成,可以看到详情中有十条消息



![image-20230811100712457](C:\Users\wang1\AppData\Roaming\Typora\typora-user-images\image-20230811100712457.png)



- 消费者代码

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
/**
 * description 消费者
 *
 * @author: woldier wong
 * @date: 2023/8/11$ 10:10$
 */
public class MQ_01_WorkQueueConsumer {
    /**
     * 步骤如下
     * 1.创建连接工厂
     * 2.设置连接工厂连接参数
     * 3.通过工厂对象创建连接
     * 4.创建channel
     * 5.channel创建queen
     * 6.接收消息
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, TimeoutException {
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
        channel.queueDeclare("work-queues",true,false,false,null);


        //* 6.接收消息
        /**
         Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag and specified arguments. Provide access to basic.deliver, basic.cancel and shutdown signal callbacks (which is sufficient for most cases). See methods with a Consumer argument to have access to all the application callbacks.
         Params:
         queue – the name of the queue
         autoAck – true if the server should consider messages acknowledged once delivered; false if the server should expect explicit acknowledgements
         arguments – a set of arguments for the consume
         deliverCallback – callback when a message is delivered
         cancelCallback – callback when the consumer is cancelled
         shutdownSignalCallback – callback when the channel/connection is shut down
         Returns:
         the consumerTag generated by the server
         Throws:
         IOException – if an error is encountered
         Since:
         5.0
         See Also:
         AMQP.Basic.Consume, AMQP.Basic.ConsumeOk, basicAck, basicConsume(String, boolean, String, boolean, boolean, Map, Consumer)
         */
        //String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;
        DefaultConsumer consumer = new DefaultConsumer(channel){
            /**
             * 实现回调方法 (原类中的回调方法为空实现)
             * @param consumerTag
             * @param envelope
             * @param properties
             * @param body
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    System.out.println("body = " +new String(body));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    //auto ack 设置为True 会自动确认 ,设置为false需要手动进行确认 在handleDelivery中进行确认
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        channel.basicQos(1);//设置每次只取一条数据
        //auto ack 设置为True 会自动确认 ,设置为false需要手动进行确认 在handleDelivery中进行确认
        channel.basicConsume("work-queues",false,consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```

消费者设置了手动确认,需要在handleDelivery回调执行成功后手动确认

为了模拟多消费者,需要开启`mutiple instance`,开启后运行两次则会开启两个实例(模拟多消费者)

![image-20230811101514346](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F90896e4b3d2cb584803e49bdec3639fc.png)



查看控制台的运行结果

![image-20230811102341496](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Fb249a8eb7d177234621fc3d8aef64c8a.png)

![image-20230811102349875](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Fe2641912e3b06f1d597275aa402b770e.png)

我们可以发现,两个实例时轮询来做的

这也和官网doc的描述一致

> ## Round-robin dispatching
>
> One of the advantages of using a Task Queue is the ability to easily parallelise work. If we are building up a backlog of work, we can just add more workers and that way, scale easily.
>
> First, let's try to run two worker instances at the same time. They will both get messages from the queue, but how exactly? Let's see.
>
> You need three consoles open. Two will run the worker program. These consoles will be our two consumers - C1 and C2.
>
> **By default, RabbitMQ will send each message to the next consumer, in sequence. On average every consumer will get the same number of messages. This way of distributing messages is called round-robin. Try this out with three or more workers.**



- Fair dispatch

>You might have noticed that the dispatching still doesn't work exactly as we want. For example in a situation with two workers, when all odd messages are heavy and even messages are light, one worker will be constantly busy and the other one will do hardly any work. Well, RabbitMQ doesn't know anything about that and will still dispatch messages evenly.
>
>在真实的生产环境中,分发的消息可能并不会像我们所期待的那样轮询工作. 打个比方(在有两个consumer的情况下), 奇数序号的消息是heavy的而偶数序号的消息都是light(轻量)的,  那么消费奇数序号的线程就会非常忙碌, 而消费偶数序号的consumer就会非常的空闲. 这是因为mq并不知道这件事, 任然一股脑的把queue中的消息发送给consumer. 
>
>This happens because RabbitMQ just dispatches a message when the message enters the queue. It doesn't look at the number of unacknowledged messages for a consumer. It just blindly dispatches every n-th message to the n-th consumer.
>
>这种情况的发生是由于mq只是将queue中的消息发送给了consumer. 这个过程mq不会去关注consumer未确认消费的信息. mq只是盲目的分发每个消息给了consumer.
>
>In order to defeat that we can use the **basicQos** method with the **prefetchCount = 1** setting. **This tells RabbitMQ not to give more than one message to a worker at a time**. Or, in other words, don't dispatch a new message to a worker until it has processed and acknowledged the previous one. Instead, it will dispatch it to the next worker that is not still busy.
>
>为了解决这一问题, 我们可以使用basicQos方法 设置投递数量为1, 这使得mq知晓了一次性不要投递超过一条数据给consumer. 换句话说, 不要投递新的信息给消费者, 直到消费者已经消费了之前到达消费者的消息, 并且告知mq已经消费. 如果一个consumer任然在消费消息, 或者还没ack告知mq, 那么mq就会把消息发送给其他空闲的消费者. 
>
>```java
>int prefetchCount = 1;
>channel.basicQos(prefetchCount);
>```
>
>

