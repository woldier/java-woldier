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

        
        channel.queueDeclare("hello_world",true,false,false,null);

        // * 6.发送消息
      
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
        channel.queueDeclare("hello_world", true, false, false, null);
        //* 6.接收消息
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            /**
             * 实现回调方法 (原类中的回调方法为空实现)
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
        channel.queueDeclare("work-queues",true,false,false,null);
        // * 6.发送消息
       
        String msg = "hello rabbit ";
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

       
        channel.queueDeclare("work-queues",true,false,false,null);


        //* 6.接收消息
        
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

>在真实的生产环境中,分发的消息可能并不会像我们所期待的那样轮询工作. 打个比方(在有两个consumer的情况下), 奇数序号的消息是heavy的而偶数序号的消息都是light(轻量)的,  那么消费奇数序号的线程就会非常忙碌, 而消费偶数序号的consumer就会非常的空闲. 这是因为mq并不知道这件事, 任然一股脑的把queue中的消息发送给consumer. 
>
>这种情况的发生是由于mq只是将queue中的消息发送给了consumer. 这个过程mq不会去关注consumer未确认消费的信息. mq只是盲目的分发每个消息给了consumer.
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

>在上一个小节，介绍了将消息分发给众多consumer中的一个。这一小节，将介绍将一条消息发送给多个消费者，换句话说，有多个消费者收到了同一条消息。这就是“发布/订阅”模式

![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F0af579781bd80221e48f5bb76e743d8b.png)

本模式的使用场景是：

假设现在有一个订单业务，用户下单之后，要通知仓库模块挑拣货物，同时需要通知快递模块生成快递订单。那么就需要两个业务模块同时收到用户下单成功的消息。





>
>
>在之前的示例中我们直接从queue中发送和接受消息。现在，将介绍消息模型的完全体。
>
>
>
>如下展示了之前示例中包含的部分：
>
>- 一个生产者应用生成并且发送消息到mq
>- 一个用于缓冲消息的队列
>- 一个用于接受消息的消费者
>
>rabbitmq中最核心的思想是生产者从来不会直接发生任何消息到消息队列中。实际上， 生产者甚至都不知道自己产生的消息会被那些队列锁接收。
>
>除此之外，生产者（producer）只能发送消息到交换机中（exchange）。一个交换机可以理解为其一边接受来自生产者producer的消息另一边则将消息放入对应的队列中。 交换机必须知道它需要对接收到的消息做什么。 收到的消息是否需要放入到特定的队列中吗？是否需要放入特定的多个队列中吗。或者他应该被丢弃吗。这都取决于交换机的类型。
>
>
>
>这里有许多可以使用的交换机类型:**direct**, **topic**, **headers** 和**fanout**. 我们将注意力放在最后一种--fanout(扇出).让我们创建一个该类型的交换机, 并且将该交换机称为`test_fanout`:
>
>```java
>channel.exchangeDeclare("test_fanout",BuiltinExchangeType.FANOUT,true,true,false,null);
>```
>
>fanout交换机非常的简单, "人如其名", 它就是简单的将消息广播到所有他知晓的queue. 这种特性也正是我们需要的.



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
> 
>
> 在前面的项目中，我们需要注意在生产中和消费者声明是同一个的，但是这样的方式很容易出错。
>
> 
>
> 如今，这种繁琐的事情不再会困扰我们了！我们期望不同模块的消费者（这里指的仓库模块和快递模块）收到所有的消息，而不是消息的子集。除此之外，我们也仅仅是对当前的信息感兴趣，而不是旧的数据。为了解决这一问题，我们需要做到两件事情。
>
> 
>
> 首先，无论什么时候我们连接到mq，我们需要一个干净的，空的队列。为了到达目的，我们创建了一个名字随机，或者更好（避免不同实例创建出同样的随机名字），我们让mq为我们挑选一个名字。
>
> 
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
            channel.exchangeDeclare("test_fanout", BuiltinExchangeType.FANOUT, true, true, false, null);


            // * 6.发送消息
       
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



![image-20230811172901673](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F13b3acd408de09ca86349a25c1befa89.png)

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

        channel.exchangeDeclare("test_fanout",BuiltinExchangeType.FANOUT,true,true,false,null);

//        channel.queueDeclare("fanout-queues",true,false,false,null);
//        channel.queueBind("fanout-queues","test_fanout","");

        String queueName = channel.queueDeclare().getQueue(); //new一个随机的队列
        channel.queueBind(queueName,"test_fanout","");  //绑定到交换机
        log.debug("queue的名称是{}",queueName);
        //* 6.接收消息
       
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

![image-20230811173333737](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F7d5181a61bd71ef5b3ca50c93bccd603.png)

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



> 
>
> 前面的fanout模式，所有绑定到交换机的队列都可以接收到消息。
>
> 
>
> 在本小节中，我们将会添加新的特性。我们使得订阅消息的子集成为可能。举个例子，在日志系统，我们仅仅将致命错误的日志文件发送给刷盘服务将错误信息持久化到硬盘中，与此同时，所有的log信息我们都发送的日志控制台打印系统进行打印。

- bindings



前文的示例中我们已经创建过队列与交换机的绑定了,编码方式如下:

```java
channel.queueBind(queueName, EXCHANGE_NAME, "");
```

 描述了交换机与queue的关系。这种关系可描述为该queue对交换机中的消息感兴趣。



bind将交换机与queue绑定的同时，可以携带额外的routing key参数。 为了避免与接收消息方法**basic_publish**的参数产生混淆，我们将其称为 **binding key**。

```java
channel.queueBind(queueName, EXCHANGE_NAME, "black");
```



binding key 的含义取决于交换机的类型。 对于fanout交换机来说， 其就是粗暴的忽略了该属性。

- Direct exchange



上一小节的日志系统广播所有的消息到所有的消费者。 我希望扩展这一特性，并且根据日志信息的重要性进行过滤。举个例子， 我们可能期望项目将error日志写入磁盘，而对于info与waring级别的则不需要。



如果还是使用fanout虚拟机，那么他不能提供这一特性。--因为他只是无脑进行广播。



因此我们使用direct交换机。dirict交换机背后的路由算法也非常的简单，消息会送往绑定的binding key 与routing key一致的queue。

![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F2e29a3a41b01b7683c9f0f3b196e05db.png)



在这里，我们可以观察到，direct交换机X和两个queue进行了绑定。第一个quque绑定的binding key是orange，迪哥哥交换机绑定了两个分别是black和green。

在这种情况下，routing key是orange的消息将只会发送给Q1。 routing key 是black和green的将会发送给Q2。

- Multiple bindings



![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Fec73e332afb8178fe3a5c6f8eac5e31e.png)



用同样的binding key绑定多个queue是绝对合法的。在上图的示例中我们可以使用相同的routing key（此处为black）将Q1，Q2与交换机X绑定。 因此，在这种情况下direct交换机表现的就像是fanout将消息广播到所有的匹配的queue中。



- 生产者代码

```java
package com.woldier;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
public class MQ_04_Routing {
    final static String EXCHANGE_NAME = "test_direct";
    final static String ROUTING_KEY[] = {"info","warn","error"};
    final static int ROUTING_KEY_POS = 2;
    /**
     * 步骤如下
     * 1.创建连接工厂
     * 2.设置连接工厂连接参数
     * 3.通过工厂对象创建连接
     * 4.创建channel
     * 5.channel声明exchange(direct)
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
        // * 5.设置exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, true, false, null);


        // * 6.发送消息
 
        String msg = "hello rabbit fanout";
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY[ROUTING_KEY_POS], null, msg.getBytes());


        /*
        释放资源
         */
        channel.close();
        connection.close();
    }
}

```



![image-20230811190647934](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F880516c6acee27481c407ea6c7d60643.png)

- 消费者

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
/**
*
* description direct exchange mode
*
* @author: woldier wong
* @date: 2023/8/11 18:52
*/
public class MQ_04_RoutingAllConsumer {
    final static String EXCHANGE_NAME = "test_direct";
    final static String ROUTING_KEY[] = {"info","warn","error"};
    final static String QUEUE_NAME = "direct_all_queue";
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

      
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, true, false, null);

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        /**
         * 对所有routing进行绑定
         */
        for (String s : ROUTING_KEY) {
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, s);
        }
        //* 6.接收消息
   
        DefaultConsumer consumer = new DefaultConsumer(channel) {
      
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    System.out.println("body = " + new String(body));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //auto ack 设置为True 会自动确认 ,设置为false需要手动进行确认 在handleDelivery中进行确认
                    //channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        
        channel.basicConsume(QUEUE_NAME, true, consumer);

        
        while (true) ;
    }
}

```



```java
package com.woldier;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MQ_04_RoutingErrConsumer {
    final static String EXCHANGE_NAME = "test_direct";
    final static String ROUTING_KEY[] = {"info","warn","error"};
    final static int ROUTING_KEY_POS = 2;
    final static String QUEUE_NAME = "direct_error_queue";
    
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

        channel.exchangeDeclare(EXCHANGE_NAME,BuiltinExchangeType.DIRECT,true,true,false,null);

        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY[ROUTING_KEY_POS]);
        //* 6.接收消息
        
        DefaultConsumer consumer = new DefaultConsumer(channel){
           

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    System.out.println("body = " +new String(body));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {

                }
            }
        };

        channel.basicConsume(QUEUE_NAME,true,consumer);

        while (true);
    }
}

```



![image-20230811191054637](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F0ec7e58ff626ee95ca3eaea7cede6f29.png)

观察可以发现，发送的一条error的消息out的数目是2，即被两个服务都接收到了



## 1.5 topic

- preliminary

在上一小节中，我们升级了日志系统（根据日志的级别进行分发）。不像fanout交换机那样只能蠢萌蠢萌的广播， 我们使用direct模式，实现了选择性的接受消息。

虽然，使用direct模式提升了系统的业务逻辑性， 但是这种模式任然存在缺点 - 他不能根据多个标准进行路由

在当前的日志系统中，我们可能希望不只是根据严重程度订阅消息，还希望根据产生消息的源头订阅消息。或许已经从 syslog unix 工具中了解到这一概念，该工具根据严重性（info/warn/crit...）和设施（auth/cron/kern...）路由日志。



这种方式将会给予我们更多的灵活性-我们可能希望只是监听来自cron的critical errors并且也监听所有来自于kern的

为了实现该功能，我们需要了解更复杂的topic交换机

- topic exchange



送入到topic交换机的消息不能是持有一个随机的routing key - 它必须是词数组，通过 `.` 来进行区分。区分的每一个词可以是任意的， 但是通常来讲词的含义与消息的特性有着某种联系。这里给出了一些routing key的例子：`stock.usd.nyse`,`nyse.vmw` , `quick.orange.rabbit`。 Routing key中的单词数目可以任意多，但是总长度不能超过255bytes



binding key（交换机与queue绑定时使用的key）也是遵循这一格式，topic交换机背后的逻辑与direct交换机类似-一条特定的routing key的消息将会被投递给所有满足binding key 的queue。然而，对于binding key这里有两件非常重要的事情。

-  *(star) 代表着确定的一个单词
- \# (hash)  则代表0个或者多个单词

![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F0992aa285420a7160b79df15253b00cd.png)



在这个例子中，我们将会发送描述动物的消息。消息将会与有三个词（两个点）的routing key一起发送。 Routing key中的第一个字段描述了速度，第二个字段描述了颜色，第三个字段描述了种族: **"<speed>.<colour>.<species>".**

- 

随后，创建了三个绑定关系：Q1绑定的routing key是 "*.orange.*"，Q2绑定的是 "*.*.rabbit" 和 "lazy.#"

这种绑定关系可以总结为如下:

Q1对orange动物感兴趣，Q2期望收到任何关于rabbit的信息以及所有关于lazay属性的信息。

routing key 为"quick.orange.rabbit"的消息将会被投递给两个queue。 "lazy.orange.elephant" 也是。然而"quick.orange.fox"将只会投递给Q1， "lazy.brown.fox"则只会投递给Q2.

"lazy.pink.rabbit"也仅仅只会投递给Q2一次（虽然他满足了两种条件）。

"quick.brown.fox"不满足任何一个binding key 因此此routing key的信息将会被丢弃掉。

如果我们打破之前设定的词组结构，发送只含有一个单词的或者四个单词的routing key，比如说 "orange"或者"quick.orange.new.rabbit"？显然，如果这个routing key 不满足任意一个binding key，那么这条消息就会被丢弃。

- provier

```java
package com.woldier;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Routing(topic)
 * https://www.rabbitmq.com/tutorials/tutorial-five-java.html
 */
public class MQ_05_Topic {
    final static String EXCHANGE_NAME = "test_topic";
    /**
     * 注意error.# 可以匹配error.ksksk.ksk 可以是长度为n的
     */
    final static String ROUTING_KEY[] = {"info.*","warn.*.woldier","error.#"};
    final static String QUEUE_NAME[] = {"topic_info_*_queue","topic_warn_woldier_queue","topic_error_#_queue"};
    final static String PUBLISH_ROUTING_KEY[] = {"info.ww","warn.xx.woldier","error.ksksk.ksk"};
    final static int PUBLISH_ROUTING_KEY_POS = 1;
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
        // * 5.设置exchange
 
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true, true, false, null);

        /**
         * 绑定queue
         */
        for (int i= 0;i< QUEUE_NAME.length;i++) {
            String s = QUEUE_NAME[i];
            channel.queueDeclare(s,true,false,true,null);
            /**
             * 若要订阅更多的topic 在这里进行多次绑定即可
             */
            channel.queueBind(s,EXCHANGE_NAME,ROUTING_KEY[i]);
        }

        // * 6.发送消息
        String msg = "hello rabbit topic";
        channel.basicPublish(EXCHANGE_NAME, PUBLISH_ROUTING_KEY[PUBLISH_ROUTING_KEY_POS], null, msg.getBytes());


        /*
        释放资源
         */
        channel.close();
        connection.close();
    }
}

```

- consumer

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MQ_05_TopicConsumer_01 {
    final static String EXCHANGE_NAME = "test_topic";
    /**
     * 注意error.# 可以匹配error.ksksk.ksk 可以是长度为n的
     */
//    final static String ROUTING_KEY[] = {"info.*","warn.woldier","error.#"};
    final static String QUEUE_NAME[] = {"topic_info_*_queue","topic_warn_woldier_queue","topic_error_#_queue"};
    final static int QUEUE_POS = 0;
   
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


        //* 6.接收消息
        
        DefaultConsumer consumer = new DefaultConsumer(channel){
           
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
        channel.basicConsume(QUEUE_NAME[QUEUE_POS],true,consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```



```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Routing(topic)
 * https://www.rabbitmq.com/tutorials/tutorial-five-java.html
 */
public class MQ_05_TopicConsumer_02 {
    final static String EXCHANGE_NAME = "test_topic";
    /**
     * 注意error.# 可以匹配error.ksksk.ksk 可以是长度为n的
     */
//    final static String ROUTING_KEY[] = {"info.*","warn.woldier","error.#"};
    final static String QUEUE_NAME[] = {"topic_info_*_queue","topic_warn_woldier_queue","topic_error_#_queue"};
    final static int QUEUE_POS = 1;
  
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


        //* 6.接收消息
        
        DefaultConsumer consumer = new DefaultConsumer(channel){
            
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
        channel.basicConsume(QUEUE_NAME[QUEUE_POS],true,consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```



```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Routing(topic)
 * https://www.rabbitmq.com/tutorials/tutorial-five-java.html
 */
public class MQ_05_TopicConsumer_03 {
    final static String EXCHANGE_NAME = "test_topic";
    /**
     * 注意error.# 可以匹配error.ksksk.ksk 可以是长度为n的
     */
//    final static String ROUTING_KEY[] = {"info.*","warn.woldier","error.#"};
    final static String QUEUE_NAME[] = {"topic_info_*_queue","topic_warn_woldier_queue","topic_error_#_queue"};
    final static int QUEUE_POS = 2;
    
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


        //* 6.接收消息
       
        DefaultConsumer consumer = new DefaultConsumer(channel){
            
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
        channel.basicConsume(QUEUE_NAME[QUEUE_POS],true,consumer);

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```



## 1.6 Remote procedure call (RPC)

> 
>
> 在1.2 我们学习了则不能使用work queue 来分发耗时任务到workers。
>
> 
>
> 但是如果是我们需要运行一个远程方法并且等待其返回。
>
> 
>
> 在这个小节，我们将使用rabbitmq 来构建一个RPC系统：一个客户端和一个可拓展的RPC服务。由于我们没有可用于分发的真实的耗时的task，在这里我们创建了一个虚假的返回斐波拉契数的RPC服务。
>
> 

- client interface



为了解释RPC服务应该怎么使用，我们将创建一个简单的客户端类。 该方法暴露出了一个名为call的方法，该方法发起了一个RPC请求并且阻塞等待直到结果返回。

```java
FibonacciRpcClient fibonacciRpc = new FibonacciRpcClient();
String result = fibonacciRpc.call("4");
System.out.println( "fib(4) is " + result);
```

> #### A note on RPC
>
> 
>
> 虽然RPC是计算时常用的模式，但是却饱受诟病。当程序员不知道调用的方法（function）是本地的耗时方法还是远程调用时，问题就出现了。这样的混淆导致了系统的不可预测性，并且增加了调试的难度。滥用RPC非但不会简化应用，并且还会带来无法挽回的“垃圾”代码。
>
> Bearing that in mind, consider the following advice: 
>
> - 确认的知道某个方法到底是本地方法还是远程调用
> - 规范的系统文档，使得组建之间的依赖关系非常的清楚。
> - 错误处理，当RPC长期不可用时，客户端应该如何响应？
>
> 如有疑问，应该避免使用 RPC。如果可以，应该使用异步流水线--将结果异步推送到下一个计算阶段，而不是类似 RPC 的阻塞。

- callback queue

总的来说，使用RabbitMq来作为远程调用的中间件是非常容易的。一个客户端发送一条请求的message，随后一个服务提供者回复消息。为了收到响应，我们需要随请求发送一个 "回调 "队列地址。我们可以使用默认队列（这在 Java 客户端中是独有的）。让我们试试看：

```java
import com.rabbitmq.client.AMQP.BasicProperties;

allbackQueueName = channel.queueDeclare().getQueue(); //定义一个临时队列

BasicProperties props = new BasicProperties
                            .Builder()
                            .replyTo(callbackQueueName)//指定回调时使用的队列
                            .build();

channel.basicPublish("", "rpc_queue", props, message.getBytes());

```

> #### Message properties
>
> 
>
> AMQP协议预先设定了14种随着消息一同传输的属性。其中的大多数属性非常少被使用，除了以下的几种：
>
> - deliveryMode(投递模式): 标记一个消息是持久（值为2）还是暂存（其他任何值）。如果你查看了1.2小节或许会对该属性有印象。
> - contentType: (内容的type)：用于描述编码的mine- type。举个例子，对于常用的JSOn格式编码那么应该设置此属性为application/json。
> - replyTo: ( 回复给)常用于命名为一个callback queue
> - correlationId:  用于将 RPC 响应与请求相关联。

- correlationId



针对于上面提出的方法，建议对于每一个RPC请求都创建一个callback queue。显然这样做是非常低效的，但幸运的是这里有着更好的方法--让我们为每个客户端创建一个回调队列。



这样做，随之而来产生了另一个问题，queue中已经收到的response怎么判断是属于哪一个请求的呢？这就是correlationId派上用场的时候了。对于每一个request我们将会将它设置为一个唯一的值。随后，当我们在callback queue中收到消息，我们将会查看这一属性，基于此我们就能后将request于response进行绑定。如果我们看见了一个未知的correlationId，我们将会安全的丢弃这一条消息 --这一条消息并不属于本队列。



你也许会问，为什么我们要忽略存在于callback queue中的这一条未知的消息，而不是失败返回？这是由于server一端可能存在竞争条件。虽然，这种可能性不大，但 RPC 服务器有可能在向我们发送应答后，但在发送请求确认信息前就死机了。如果出现这种情况，重新启动的 RPC 服务器将再次处理请求。这就是为什么我们必须在客户端合理地处理重复响应，而且 RPC 最好是能够确保幂等性（idempotent，无论调用多少次，结果都是一样的）



- summary

![](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Faf243910b11eb96741645b7c30154d7f.png)

Our RPC will work like this:

- ------------------------------------

  - 对于一个RPC调用，客户端携带两个额外参数发送一条消息，replyTo来为请求request设置一个anonymous独占队列，correlationId用来为每一个请求设置一个唯一的值。
  - request请求将被送往 rpc_queue （名称来自上图）队列
  -  RPC的worker（又称为server）随时等待接受来自于队列中的请求。当一个请求出现，server**处理请求并且根据请求中的replyTo发送response消息到客户端，
  -  客户端等待回复队列中的消息。当一条消息出现，检查correlationId字段。如果与请求的参数一致，那么返回response给应用。



- server

```java
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

```







- client

```java
package com.woldier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
/**
*
* description RPC 服务端
*
* @author: woldier wong
* @date: 2023/8/12 14:27
*/
public class MQ_06_RPCServer {

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }
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
//        void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments) throws IOException;
        channel.queueDeclare("rpc-queues",true,false,false,null);
        channel.queuePurge("rpc-queues");
        //* 6.接收消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);

                System.out.println(" [.] fib(" + message + ")");
                response += fib(n);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicQos(1);//设置每次只取一条数据
        //auto ack 设置为True 会自动确认 ,设置为false需要手动进行确认 在handleDelivery中进行确认
        channel.basicConsume("rpc-queues",false,deliverCallback,e->{});

        /**
         * 最后不需要关闭channel 和 connection 因为需要持续监听
         */
        while (true);
    }
}

```

server端打印的日志

```shell
 [.] fib(11)
 
 Process finished with exit code 0
```

client端打印的日志

```java
result = 89

Process finished with exit code 0
```

## 1.7 publisher confirm

> 
>
> Publisher confirms 是rabbitmq扩展的可靠发布的一个实现。当publisher confirms在channel中被开启时，客户端的消息发布者发布的消息被broker（代理）进行异步 的确认，这意味着

- overview

在这一小节中，我们将会使用publisher confirm（发布者确认）模式来确保发布的消息安全的到达了broker。本小节将介绍发布者确认模式中的几种策略并且解释他们的优缺点。

- Enabling Publisher Confirms on a Channel

发布者确认模式是rabbitmq对AMQP 0.9.1协议的拓展， 因此这个特性并没有被默认开启。 发布者确认模式可以在channel下通过以下代码开启：

```java
Channel channel = connection.createChannel();
channel.confirmSelect();
```



在每个期望进行发布者确认的channel都必须启用这一选项。启用的次数仅仅需要一次，而不是每次发送消息都需要。

- Strategy #1: Publishing Messages Individually

让我们从最简单的publish confirm开始，发布一条消息，然后异步的等待其异步确认。

```java
while (thereAreMessagesToPublish()) {
    byte[] body = ...;
    BasicProperties properties = ...;
    channel.basicPublish(exchange, queue, properties, body);
    // uses a 5 second timeout
    channel.waitForConfirmsOrDie(5_000);
}
```

在之前的例子中，我们发布了一条消息并且通过`Channel#waitForConfirmsOrDie(long)`等待确认。 一但消息被确认该方法立即返回。如果消息在达到超时时间之前一直未被确认，或者说这条消息“死掉了”（出于某种原因broker不能够关注到该条消息），那么该方法就会抛出异常。对于异常的处理常常包括记录发送消息失败的日志或者是重试发送消息。

不用的客户端（不同的语言）依赖库有着不同的方式来异步执行发布者确认，因此确保仔细的查阅了你所使用的客户端的文档。

发布者确认技术的原理非常简答明了，不过仍然存在缺陷：其直接导致了publishing的速度变慢了，因为当前正在阻塞等待确认的消息阻碍了后续的消息的发送。这种方式的吞吐量不会超过几百条数据每秒。尽管如此，这种方式对于一些应用的需求来说是足够的。

> #### Are Publisher Confirms Asynchronous? 发布者是异步确认的吗？
>
> 我们在开始的时候提到broker异步确认成功发步消息，但是在第一个示例中，代码同步的阻塞等待知道消息被确认发布了。客户端实际上是收到了异步的确认发布然后在唤醒阻塞的`waitForConfirmsOrDie`方法调用. 把waitForConfirmsOrDie当作是一个同步辅助方法，它的底层原理就是异步通知。

- trategy #2: Publishing Messages in Batches

为了优化前面的示例，我们可以发布一批次的消息并且等待这一个batch的消息被确认。接下的示例使用了含有100条数据的batch。

```java
int batchSize = 100;
int outstandingMessageCount = 0;
while (thereAreMessagesToPublish()) {
    byte[] body = ...;
    BasicProperties properties = ...;
    channel.basicPublish(exchange, queue, properties, body);
    outstandingMessageCount++;
    if (outstandingMessageCount == batchSize) {
        channel.waitForConfirmsOrDie(5_000);
        outstandingMessageCount = 0;
    }
}
if (outstandingMessageCount > 0) {
    channel.waitForConfirmsOrDie(5_000);
}
```



与等待单条消息的确认相比，等待一批消息的确认可大幅提高吞吐量（远程 RabbitMQ 节点的确认次数可达 20-30 次）。这种方法的一个缺点是，如果出现故障，我们不知道到底是哪里出了问题，因此可能需要在内存中保留整个批处理，以便记录一些有意义的信息或重新发布报文。而且这种解决方案仍然是同步的，因此会阻止信息的发布。

- Strategy #3: Handling Publisher Confirms Asynchronously

broker异步的确认发布消息，那么只需要在客户端中注册一个回调以通知这条发布的消息被确认了：

```java
Channel channel = connection.createChannel();
channel.confirmSelect();
channel.addConfirmListener((sequenceNumber, multiple) -> {
    // code when message is confirmed
}, (sequenceNumber, multiple) -> {
    // code when message is nack-ed
});
```



这里有两个回调，一个用于成功被确认的消息，另一个用户没有被ack的消息（这样的消息可能是被broker丢失了）。每个回调方法都有两个参数：

-  一个用于区分消息的数。后面将介绍怎么讲其与发布的消息联系起来。

传递的是一个boolean3。如果返回false，那么只有一条消息被确认或者non-ack，如果值为true，所有等于或者是小于当前sequence number的消息都被成功的接受到了（类似于计算机网络中的滑动窗口算法）

sequence number 的值可以通过`Channel#getNextPublishSeqNo()`方法获得。

```java
int sequenceNumber = channel.getNextPublishSeqNo());
ch.basicPublish(exchange, queue, properties, body);
```

将消息与sequence number联系起来的简单办法是通过map。假设我们想要发送一条string消息（发送string消息是因为它可以方便的转换为byte数组用于发布）。这里给出了使用map结构来将message与sequence number联系起来的简单代码示例：

```java
ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
// ... code for confirm callbacks will come later
String body = "...";
outstandingConfirms.put(channel.getNextPublishSeqNo(), body);
channel.basicPublish(exchange, queue, properties, body.getBytes());
```

现在，发布代码通过一个map来跟踪出站消息。我们需要在确认信息到达时清理该映射，并在信息被删除时发出警告：

```java
ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
ConfirmCallback cleanOutstandingConfirms = (sequenceNumber, multiple) -> {
    if (multiple) {
        ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(
          sequenceNumber, true
        );
        confirmed.clear();
    } else {
        outstandingConfirms.remove(sequenceNumber);
    }
};

channel.addConfirmListener(
  cleanOutstandingConfirms, //第一个，成功的回调
  (sequenceNumber, multiple) -> { //第二个，失败的回调，在删除map中对应的key之前，应该做一些额外的处理
    String body = outstandingConfirms.get(sequenceNumber);
    System.err.format(
      "Message with body %s has been nack-ed. Sequence number: %d, multiple: %b%n",
      body, sequenceNumber, multiple
    );
    cleanOutstandingConfirms.handle(sequenceNumber, multiple);
});
// ... publishing code
```



上面的示例介绍了当一个消息被确认后将其从map中删除。注意到这个回调同时处理单信息或者多信息确认。这个回调将会在确认到达时被调用（` Channel#addConfirmListener`的第一个参数）。non-acked消息确认会消息的body并且发出警告。随后重用之前的回调来清理map中对应的为完成确认的message（无论是那种情况map中对应的实体都会被清除）

> #### How to Track Outstanding Confirms? 怎么跟踪未完成确认的消息呢？
>
> 
>
> 示例中使用的是`ConcurrentNavigableMap`来追踪未完成的确认消息。使用这样的数据结构是基于几方面的考量。map结构使得可以方便的将sequence number与message进行关联（无论消息内容是什么）并且可以很方便的根据给出的sequence number从map中**<u>移除id小于对应值的实体</u>**（处理多消息确认情况）。最后，其支持并发访问，因为确认回调方法被一个线程调用，而这个线程一直与发布消息的线程都是不一致的。
>
> 
>
> 这里也有其他的方法来追踪未完成确认的消息，而不是通过一个繁杂的map实现，比如使用简单的concurrent hash map（虽然也很复杂）以及一个变量来跟踪publishing sequence的下界，不过这种方法比较复杂，这里只是提供思路。





小结一下，异步处理 publisher confirm常常需要以下的步骤：

- 提供一个方法关联publishing sequence number与message

- 在通道上注册一个confirm监听器用来接受发布者消息的ack/non-ack时的异步通知，执行相关的操作，如记录或者重新发布被non-ack的消息。在此步骤中可能能还需要对存储的序列号与信息关联的信息进行清理
- 在发布前跟踪sequence number 



