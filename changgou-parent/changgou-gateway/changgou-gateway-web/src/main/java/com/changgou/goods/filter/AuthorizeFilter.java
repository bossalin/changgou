package com.changgou.goods.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器
 * 实现用户权限鉴别（校验）
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /***
     * 全局过滤器
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /**
         *   1.获取用户令牌信息
         *      1）头文件中
         *      2）参数获取令牌
         *      3）Cookie中
         *   2.如果没有令牌，则拦截
         *   3.如果有令牌，则校验令牌是否有效
         *   4.无效则拦截
         *   5.有效则放行
         */

        //获取Request、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //用户如果是登录或者一些不需要做权限认证的请求，直接放行
        String uri = request.getURI().getPath();
        if (URLFilter.hasAuthorize(uri)){
            return chain.filter(exchange);
        }


        //获取请求的URI
        String path = request.getURI().getPath();

        //如果是登录、goods等开放的微服务[这里的goods部分开放],则直接放行,这里不做完整演示，完整演示需要设计一套权限系统
        if (path.startsWith("/api/user/login") || path.startsWith("/api/brand/search/")) {
            //放行
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }
        //获取用户令牌信息
        //1.获取头文件中的令牌信息
        String tokent = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //boolean true:令牌在头文件中   flase:令牌不在头文件中 -->将令牌封装到头文件中，再传递给其他服务
        boolean hasToken = true;

        //2.如果头文件中没有，则从请求参数中获取
        if (StringUtils.isEmpty(tokent)) {
            tokent = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
        //3.Cookie
        if(StringUtils.isEmpty(tokent)){
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (httpCookie!=null){
                tokent=httpCookie.toString();
            }
            hasToken = false;
        }

        //如果没有令牌，则拦截
        if (StringUtils.isEmpty(tokent)) {
            //设置没有权限的状态码  401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();
        }

        //解析令牌数据
//        try {
//            //JwtUtil直接拷过来，不要以来conmon包，里面有好多包对其有影响
////            Claims claims = JwtUtil.parseJWT(tokent);           //现在使用RAS不能校验了
//
//            //判断令牌是否为空，如果不为空，将令牌放到头文件中，放行
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            //解析失败，响应401错误
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
        //判断令牌是否为空，如果不为空，将令牌放到头文件中，放行
        if (StringUtils.isEmpty(tokent)){
                        //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }else {
            if (!hasToken) {
                //  是否有前缀bearer，如果没有则添加前缀 bearer
                if (!tokent.startsWith("bearer ") && !tokent.startsWith("Bearer ")){
                    tokent="bearer "+tokent;
                }
                //将令牌封装到头文件中，不然后面学的OAuth2.0无法校验令牌的
                request.mutate().header(AUTHORIZE_TOKEN,tokent);
            }
        }


        //放行
        return chain.filter(exchange);
    }


    /***
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
