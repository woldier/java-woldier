package com.woldier;

import com.wolder.Main;
import org.junit.jupiter.api.Test;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@SpringBootTest(classes = {Main.class})
public class Test4RedisListener {
    @Autowired
    RedisMessageListenerContainer listenerContainer;

    @Test
    public void test() throws ExecutionException, InterruptedException {
        FutureTask<Long> task = new FutureTask<>(() -> {
            System.out.println("hello futher task");
            return 1L;
        });
//        System.out.println("创建接收器");
//        MessageListener<Long> reciver = new RedisReceiver<>(task);
//        System.out.println("定义接收管道");
//        PatternTopic topic = new PatternTopic("topic");
//        System.out.println("绑定到适配器");
//        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(reciver,"onMessage");
//        System.out.println("添加消息监听");
//        listenerContainer.addMessageListener(listenerAdapter,topic);


        task.get();
    }
   static class RedisReceiver<M,R> implements MessageListener<M>{
        private final FutureTask<R> task;

       public RedisReceiver(FutureTask<R> task) {
           this.task = task;
       }

       /**
         * Invokes on every message in topic
         *
         * @param channel of topic
         * @param msg     topic message
         */
        @Override
        public void onMessage(CharSequence channel, M msg) {
            System.out.println("channel = " + channel + ", msg = " + msg);
            new Thread(task).start();
        }
    }
}
