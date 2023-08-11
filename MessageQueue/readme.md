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

## 1.3 publish subscribe



Sending messages to many consumers at once(一次性将同一条消息发送给多个consumer)

>In the **1.2** we created a work queue. The assumption behind a work queue is that each task is delivered to exactly one worker. In this part we'll do something completely different -- we'll deliver **a message** to **multiple consumers**. This pattern is known as "publish/subscribe".
>
>在上一个小节，介绍了将消息分发给众多consumer中的一个。这一小节，将介绍将一条消息发送给多个消费者，换句话说，有多个消费者收到了同一条消息。这就是“发布/订阅”模式

![](https://www.rabbitmq.com/img/tutorials/exchanges.png)

本模式的使用场景是：

假设现在有一个订单业务，用户下单之后，要通知仓库模块挑拣货物，同时需要通知快递模块生成快递订单。那么就需要两个业务模块同时收到用户下单成功的消息。





>
>
>In previous parts of the tutorial we sent and received messages to and from a queue. Now it's time to introduce the full messaging model in Rabbit. 在之前的示例中我们直接从queue中发送和接受消息。现在，将介绍消息模型的完全体。
>
>Let's quickly go over what we covered in the previous tutorials:
>
>如下展示了之前示例中包含的部分：
>
>- A *producer* is a user application that sends messages.一个生产者应用生成并且发送消息到mq
>- A *queue* is a buffer that stores messages.一个用于缓冲消息的队列
>- A *consumer* is a user application that receives messages.一个用于接受消息的消费者
>
>The core idea in the messaging model in RabbitMQ is that the producer never sends any messages directly to a queue. Actually, quite often the producer doesn't even know if a message will be delivered to any queue at all.
>
>rabbitmq中最核心的思想是生产者从来不会直接发生任何消息到消息队列中。实际上， 生产者甚至都不知道自己产生的消息会被那些队列锁接收。
>
>Instead, the producer can only send messages to an *exchange*. An exchange is a very simple thing. On one side it receives messages from producers and the other side it pushes them to queues. The exchange must know exactly what to do with a message it receives. Should it be appended to a particular queue? Should it be appended to many queues? Or should it get discarded. The rules for that are defined by the *exchange type*.
>
>除此之外，生产者（producer）只能发送消息到交换机中（exchange）。一个交换机可以理解为其一边接受来自生产者producer的消息另一边则将消息放入对应的队列中。 交换机必须知道它需要对接收到的消息做什么。 收到的消息是否需要放入到特定的队列中吗？是否需要放入特定的多个队列中吗。或者他应该被丢弃吗。这都取决于交换机的类型。
>
>There are a few exchange types available: **direct**, **topic**, **headers** and **fanout**. We'll focus on the last one -- the fanout. Let's create an exchange of this type, and call it test_fanout:
>
>```java
>channel.exchangeDeclare("test_fanout",BuiltinExchangeType.FANOUT,true,true,false,null);
>```
>
>The fanout exchange is very simple. As you can probably guess from the name, it just broadcasts all the messages it receives to all the queues it knows. And that's exactly what we need for our task.



> **tips**
>
> - Listing exchanges
>
> To list the exchanges on the server you can run the ever useful rabbitmqctl:
>
> ```shell
> sudo rabbitmqctl list_exchanges
> ```
>
> In this list there will be some amq.* exchanges and the default (unnamed) exchange. These are created by default, but it is unlikely you'll need to use them at the moment.
>
> - Nameless exchange
>
> In previous parts of the tutorial we knew nothing about exchanges, but still were able to send messages to queues. That was possible because we were using a default exchange, which we identify by the empty string ("").
>
> Recall how we published a message before
>
> ```java
> channel.basicPublish("", "hello", null, message.getBytes());
> ```
>
> The first parameter is the name of the exchange. The empty string denotes the default or nameless exchange: messages are routed to the queue with the name specified by **routingKey**, if it exists.



现在我们发送一条消息到刚刚创建的交换机

```java
channel.basicPublish( "test_fanout", "", null, message.getBytes());
```

> Temporary queues(临时队列)
>
> As you may remember previously we were using queues that had specific names (remember hello and task_queue?). Being able to name a queue was crucial for us -- we needed to point the workers to the same queue. Giving a queue a name is important when you want to share the queue between producers and consumers.
>
> 在前面的项目中，我们需要注意在生产中和消费者声明是同一个的，但是这样的方式很容易出错。
>
> But that's not the case for our logger. We want to hear about all log messages, not just a subset of them. We're also interested only in currently flowing messages not in the old ones. To solve that we need two things.
>
> 如今，这种繁琐的事情不再会困扰我们了！我们期望不同模块的消费者（这里指的仓库模块和快递模块）收到所有的消息，而不是消息的子集。除此之外，我们也仅仅是对当前的信息感兴趣，而不是旧的数据。为了解决这一问题，我们需要做到两件事情。
>
> Firstly, whenever we connect to Rabbit we need a fresh, empty queue. To do this we could create a queue with a random name, or, even better - let the server choose a random queue name for us.
>
> 首先，无论什么时候我们连接到mq，我们需要一个干净的，空的队列。为了到达目的，我们创建了一个名字随机，或者更好（避免不同实例创建出同样的随机名字），我们让mq为我们挑选一个名字。
>
> Secondly, once we disconnect the consumer the queue should be automatically deleted.
>
> 第二，一旦我们的消费者断开连接，对应的queue需要自动的删除掉。
>
> In the Java client, when we supply no parameters to queueDeclare() we create a non-durable, exclusive, autodelete queue with a generated name:
>
> ```java
> String queueName = channel.queueDeclare().getQueue();
> ```
>
> 



- provider

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
/**
*
* description 订阅发布模式
*
* @author: woldier wong
* @date: 2023/8/11 14:46
*/
public class MQ_03_PubSub {

        /**
         * 步骤如下
         * 1.创建连接工厂
         * 2.设置连接工厂连接参数
         * 3.通过工厂对象创建连接
         * 4.创建channel
         * 5.channel声明exchange ,创建queue
         * 6.queue与exchange绑定
         * 7.接收消息
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
            // * 5.设置exchange
            /**
             * Declare an exchange, via an interface that allows the complete set of arguments. 通过允许完整参数集的接口声明交换
             * Params:
             * exchange – the name of the exchange 交易所的名称
             * type – the exchange type 交换类型
             * durable – true if we are declaring a durable exchange (the exchange will survive a server restart) 如果我们声明了一个持久交换，则为true(该交换在服务器重启后仍然有效)
             * autoDelete – true if the server should delete the exchange when it is no longer in use 如果服务器应该在不再使用交换器时删除该交换器，则为true
             * internal – true if the exchange is internal, i.e. can't be directly published to by a client. 如果交换是内部的，即不能被客户端直接发布到
             * arguments – other properties (construction arguments) for the exchange 交换的其他属性(构造参数)
             * Returns:
             * a declaration-confirm method to indicate the exchange was successfully declared 确认方法，表示交换已成功声明
             * Throws:
             * IOException – if an error is encountered
             * See Also:
             * AMQP.Exchange.Declare, AMQP.Exchange.DeclareOk
             */
            //exchangeDeclare(String exchange, BuiltinExchangeType type,boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException;
            channel.exchangeDeclare("test_fanout", BuiltinExchangeType.FANOUT, true, true, false, null);


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
            String msg = "hello rabbit fanout";
            //void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props, byte[] body)throws IOException;
            channel.basicPublish("test_fanout", "", null, msg.getBytes());

        /*
        释放资源
         */
            channel.close();
            connection.close();
        }
}

```



![image-20230811172901673](/Users/user/Library/Application Support/typora-user-images/image-20230811172901673.png)

- consumer

启动了两个实例

```java

package com.woldier;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
/**
*
* description pubsub消费者
*
* @author: woldier wong
* @date: 2023/8/11 16:18
*/
@Slf4j
public class MQ_03_PubSubConsumer {
    /**
     * 步骤如下
     * 1.创建连接工厂
     * 2.设置连接工厂连接参数
     * 3.通过工厂对象创建连接
     * 4.创建channel
     * 5.channel声明exchange ,创建queue
     * 6.queue与exchange绑定
     * 7.接收消息
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
        // * 5.channel声明exchange ,创建queue

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
        channel.exchangeDeclare("test_fanout",BuiltinExchangeType.FANOUT,true,true,false,null);

//        channel.queueDeclare("fanout-queues",true,false,false,null);
//        channel.queueBind("fanout-queues","test_fanout","");

        String queueName = channel.queueDeclare().getQueue(); //new一个随机的队列
        channel.queueBind(queueName,"test_fanout","");  //绑定到交换机
        log.debug("queue的名称是{}",queueName);
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
                    //channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        //channel.basicQos(1);//设置每次只取一条数据
        //auto ack 设置为True 会自动确认 ,设置为false需要手动进行确认 在handleDelivery中进行确认

//        channel.basicConsume("fanout-queues",true,consumer);
        channel.basicConsume(queueName,true,consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```

![image-20230811173333737](/Users/user/Library/Application Support/typora-user-images/image-20230811173333737.png)

![image-20230811173302261](/Users/user/Library/Application Support/typora-user-images/image-20230811173302261.png)





```shell
17:26:58.000 [main] DEBUG com.woldier.MQ_03_PubSubConsumer - queue的名称是amq.gen-KCDqVu3O7GIO7pXZC5jIsw
body = hello rabbit fanout
```

```shell
17:27:19.923 [main] DEBUG com.woldier.MQ_03_PubSubConsumer - queue的名称是amq.gen--PApBCqnL_tUbay-UwfUmg
body = hello rabbit fanout
```

可以发现两个实例的queue是不一样的，但是他们收到了同样的消息。

## 1.4 routing



> In the previous tutorial we built a simple fanout system. We were able to broadcast  messages to many receivers.
>
> 前面的fanout模式，所有绑定到交换机的队列都可以接收到消息。
>
> In this tutorial we're going to add a feature to it - we're going to make it possible to subscribe only to a subset of the messages. For example, we will be able to direct only critical error messages to the log file (to save disk space), while still being able to print all of the log messages on the console.
>
> 在本小节中，我们将会添加新的特性。我们使得订阅消息的子集成为可能。举个例子，在日志系统，我们仅仅将致命错误的日志文件发送给刷盘服务将错误信息持久化到硬盘中，与此同时，所有的log信息我们都发送的日志控制台打印系统进行打印。

- bindings

In previous examples we were already creating bindings. You may recall code like:

```java
channel.queueBind(queueName, EXCHANGE_NAME, "");
```

A binding is a relationship between an exchange and a queue. This can be simply read as: the queue is interested in messages from this exchange. 

binding 描述了交换机与queue的关系。这种关系可描述为该queue对交换机中的消息感兴趣。

Bindings can take an extra **routingKey** parameter. To avoid the confusion with a **basic_publish** parameter we're going to call it a **binding key**. This is how we could create a binding with a key:

将交换机与queue绑定的同时，可以携带额外的routing key参数。 为了避免与接收消息方法**basic_publish**的参数产生混淆，我们将其称为 **binding key**。

```java
channel.queueBind(queueName, EXCHANGE_NAME, "black");
```

The meaning of a binding key depends on the exchange type. The **fanout** exchanges, which we used previously, simply ignored its value.

binding key 的含义取决于交换机的类型。 对于fanout交换机来说， 其就是粗暴的忽略了该属性。

- Direct exchange

Our logging system from the previous tutorial broadcasts all messages to all consumers. We want to extend that to allow filtering messages based on their severity. For example we may want a program which writes log messages to the disk to only receive critical errors, and not waste disk space on warning or info log messages.

上一小节的日志系统广播所有的消息到所有的消费者。 我希望扩展这一特性，并且根据日志信息的重要性进行过滤。举个例子， 我们可能期望项目将error日志写入磁盘，而对于info与waring级别的则不需要。

We were using a fanout exchange, which doesn't give us much flexibility - it's only capable of mindless broadcasting.

如果还是使用fanout虚拟机，那么他不能提供这一特性。--因为他只是无脑进行广播。

We will use a direct exchange instead. The routing algorithm behind a direct exchange is simple - a message goes to the queues whose binding key exactly matches the routing key of the message.

To illustrate that, consider the following setup:

因此我们使用direct交换机。dirict交换机背后的路由算法也非常的简单，消息会送往绑定的binding key 与routing key一致的queue。

![](https://www.rabbitmq.com/img/tutorials/direct-exchange.png)

In this setup, we can see the direct exchange X with two queues bound to it. The first queue is bound with binding key orange, and the second has two bindings, one with binding key black and the other one with green.

In such a setup a message published to the exchange with a routing key orange will be routed to queue Q1. Messages with a routing key of black or green will go to Q2. All other messages will be discarded.

在这里，我们可以观察到，direct交换机X和两个queue进行了绑定。第一个quque绑定的binding key是orange，迪哥哥交换机绑定了两个分别是black和green。

在这种情况下，routing key是orange的消息将只会发送给Q1。 routing key 是black和green的将会发送给Q2。

- Multiple bindings



![](https://www.rabbitmq.com/img/tutorials/direct-exchange-multiple.png)



It is perfectly legal to bind multiple queues with the same binding key. In our example we could add a binding between Xand Q1 with binding key black. In that case, the direct exchange will behave like fanout and will broadcast the message to all the matching queues. A message with routing key black will be delivered to both Q1 and Q2.

用同样的binding key绑定多个queue是绝对合法的。在上图的示例中我们可以使用相同的routing key（此处为black）将Q1，Q2与交换机X绑定。 因此，在这种情况下direct交换机表现的就像是fanout将消息广播到所有的匹配的queue中。



- 生产者代码

