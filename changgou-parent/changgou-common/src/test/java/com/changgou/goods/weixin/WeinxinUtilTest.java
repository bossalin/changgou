package com.changgou.goods.weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信SDK相关测试
 */
public class WeinxinUtilTest {

    /***
     * 1.生成随机字符
     * 2.将Map转成xml字符串
     * 3.将Map转成XML字符串，并生产签名
     * 4.将XML字符串转成Map
     */
    @Test
    public void testdemo() throws Exception{
        String s = WXPayUtil.generateNonceStr();
        System.out.println(s);

        //将Map转成XML字符串
        Map<String,String> dataMap=new HashMap<>();
        dataMap.put("id","No.001");
        dataMap.put("title","畅购商城商品支付");
        dataMap.put("money","999");
        //将Map转成XML字符串，并且生成签名
        String generateSignature = WXPayUtil.generateSignature(dataMap, "miyao");
        dataMap.put("sign",generateSignature);
        String toXml = WXPayUtil.mapToXml(dataMap);
        System.out.println("map转xml\n"+toXml);


        //将XML字符串转成Map
        Map<String, String> map = WXPayUtil.xmlToMap(toXml);
        System.out.println("将XML转成Map"+map);
    }
}
