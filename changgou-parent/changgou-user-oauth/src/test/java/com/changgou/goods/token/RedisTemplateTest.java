package com.changgou.goods.token;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTemplateTest.class)
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testAdd(){
        redisTemplate.boundValueOps("abc").set("uuu");
        redisTemplate.boundValueOps("qqq").set("quu");
        redisTemplate.boundValueOps("ddd").set("duu");
    }

//    @Bean
//    public RedisTemplate<String, String> redisTemplate(){
//        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
//        return redisTemplate;
//    }
}
