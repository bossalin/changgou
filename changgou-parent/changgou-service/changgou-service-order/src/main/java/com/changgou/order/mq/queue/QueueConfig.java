package com.changgou.order.mq.queue;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * 延时队列配置
 */
@Configuration
public class QueueConfig {

    /***
     * queue1 :延时队列会过期，过期后会把数据发送给queue2
     */
    @Bean
    public Queue orderDelayQueue(){
        return QueueBuilder.durable("orderDelayQueue")
                .withArgument("x-dead-letter-exchange","orderListenerExchange")  //orderDelayQueue队列信息会过期，过期之后，进入到死信队列，死信队列数据绑定到其他交换机中
                .withArgument("x-dead-letter-routing-key","orderListenerQueue")  //将死信交换机的信息路由给自己
                .build();
    }

    /***
     * queue2
     */
    @Bean
    public Queue orderListenerQueue(){
        return new Queue("orderListenerQueue",true);
    }

    /***
     * 交换机
     */
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange("orderListenerExchange");
    }


    /***
     * 队列queue2绑定交换机Exchange
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue,Exchange orderListenerExchange){
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange).with("orderListenerQueue").noargs();
    }
}
