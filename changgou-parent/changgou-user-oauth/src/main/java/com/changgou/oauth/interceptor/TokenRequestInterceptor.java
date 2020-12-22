package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    /***
     * Fegin执行之前，进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /***
         * 从数据库加载查询用户信息
         * 1.没有令牌，Feign调用之前，生成令牌
         * 2.Feign调用之前，令牌需要携带过去
         * 3.Feign调用之前，令牌需要存放到Header文件中
         * 4.请求 ->Feign调用 ->拦截器RequestInterceptor ->Fegin调用之前执行拦截
         */

        //生成管理员令牌
        String token= AdminToken.testCreateToken();
        requestTemplate.header("Authorization","bearer "+token);

    }
}
