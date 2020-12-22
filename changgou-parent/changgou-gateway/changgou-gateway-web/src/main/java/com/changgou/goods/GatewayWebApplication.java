package com.changgou.goods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class,args);
    }
    /***
     * IP限流  创建用户唯一标识 使用IP作为用户唯一标识，来根据IP进行限流操作
     * @return
     */
    @Bean(name="ipKeyResolver")
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                //获取远程客户端IP
//                String hostName = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                String hostName = exchange.getRequest().getRemoteAddress().getHostString();
                System.out.println("hostName:"+hostName);

                return Mono.just(hostName);
            }
        };
    }
}