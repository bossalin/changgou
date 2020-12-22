package com.changgou.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author wzq on 2020/11/26.
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})//该模块不调用数据库，exclude={DataSourceAutoConfiguration.class}排斥禁用数据库加载
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.changgou.goods.feign")    //开启Feign
@EnableElasticsearchRepositories(basePackages = "com.changgou.search.dao")   //扫描Elasticsearch
public class SearchApplication {

    public static void main(String[] args) {
        /**
         * Springboot整合Elasticsearch 在项目启动前设置一下的属性，防止报错
         * 解决netty冲突后初始化client时还会抛出异常
         * availableProcessors is already set to [12], rejecting [12]
         ***/
        //springboot底层使用netty，要解决netty冲突，加该设置netty不要自动启动   报错就加上
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(SearchApplication.class,args);
    }
}
