package com.changgou.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MQSeckillConfig {

    /***
     * 读取配置文件中的信息的对象
     */
    @Autowired
    private Environment environment;

    /***
     * 创建队列
     */
    @Bean
    public Queue orderSeckillQueue(){
       return new Queue(environment.getProperty("mq.pay.queue.Seckillorder"));
    }

    /***
     * 创建交换机
     */
    @Bean
    public Exchange orderSeckillExchange(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.Seckillorder"),true,false);
    }

    /***
     * 队列绑定交换机
     */
    @Bean
    public Binding orderSeckillBinding(Queue orderSeckillQueue,Exchange orderSeckillExchange){
        return BindingBuilder.bind(orderSeckillQueue).to(orderSeckillExchange).with(environment.getProperty("mq.pay.routing.Seckillkey")).noargs();
    }
}
