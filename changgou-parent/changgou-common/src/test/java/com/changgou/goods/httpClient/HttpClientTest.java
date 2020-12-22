package com.changgou.goods.httpClient;

import entity.HttpClient;
import org.junit.Test;

/****
 * httpclient使用案例
 */
public class HttpClientTest {

    /****
     * 发送http/https请求
     *      发送指定参数
     *      可以获取响应的结果
     */
    @Test
    public void testHttpClient() throws Exception{
        HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        client.setHttps(true);
        String xml="<xml><name>张三</name></xml>";
        client.setXmlParam(xml);
        //发送请求，带xml参数的一律用post请求
        client.post();
        //获取响应数据
        String result=client.getContent();
        System.out.println(result);
    }

}
