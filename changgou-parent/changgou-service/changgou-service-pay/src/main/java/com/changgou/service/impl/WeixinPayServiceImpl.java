package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    //应用ID
    @Value("${weixin.appid}")
    private String appid;
    //商户ID
    @Value("${weixin.partner}")
    private String partner;
    //秘钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    //支付回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /***
     * 创建二维码操作
     * @param paramenterMap
     * @return
     */
    @Override
    public Map createNative(Map<String, String> paramenterMap) throws Exception{
        //参数
        Map<String,String> paramMap = new HashMap<String, String>();
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        //随机字符串
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body","畅购商城商品不错");
        //订单号
        paramMap.put("out_trade_no",paramenterMap.get("out_trade_no"));
        //交易金额，单位：分
        paramMap.put("total_fee",paramenterMap.get("total_fee"));
        paramMap.put("spbill_create_ip","127.0.0.1");
        //交易结果回调通知地址
        paramMap.put("notify_url",notifyurl);
        paramMap.put("trade_type","NATIVE");

        //********获取自定义数据************
        String exchange = paramenterMap.get("exchange");
        String routingkey = paramenterMap.get("routingkey");
        Map<String,String> zidingyimap=new HashMap<>();
        zidingyimap.put("exchange",exchange);
        zidingyimap.put("routingkey",routingkey);
        //如果是秒杀订单需要传username
        String username = paramenterMap.get("username");
        if (!StringUtils.isEmpty(username)){
            zidingyimap.put("username",username);
        }

        String attach = JSON.toJSONString(zidingyimap);
        paramMap.put("attach",attach);

        //********获取自定义数据************

        //将Map转成XML字符串，并携带签名
        String generateSignedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
//        paramMap.put("sign",generateSignedXml);
//        String mapToXml = WXPayUtil.mapToXml(paramMap);
        //URL地址
        String url="https://api.mch.weixin.qq.com/pay/unifiedorder";
        //提交方式
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        //提交参数
        httpClient.setXmlParam(generateSignedXml);
        httpClient.post();
        //获取返回的数据
        String content = httpClient.getContent();

        //返回数据转成Map
        Map<String, String> map = WXPayUtil.xmlToMap(content);

        return map;
    }
    /***
     * 查询订单状态
     * @param out_trade_no : 客户端自定义订单编号
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1.封装参数
            Map<String,String> param = new HashMap<String, String>();
            param.put("appid",appid);                            //应用ID
            param.put("mch_id",partner);                         //商户号
            param.put("out_trade_no",out_trade_no);              //商户订单编号
            param.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符

            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param,partnerkey);

            //3、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
