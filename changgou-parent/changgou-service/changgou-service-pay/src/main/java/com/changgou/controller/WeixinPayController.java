package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /****
     * 支付结果通知回调
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){
        //获取网络输入流
        InputStream inStream;
        try {
            //读取支付回调数据
            inStream = request.getInputStream();
            /**
             * 之前：
             * 缓冲区 byte[] buffer = new byte[1024];
             * 创建一个outputstream --> 输入到文件
             */
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];  //定义了一个缓存区
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            // 将支付回调数据转换成xml字符串
            String result = new String(outSteam.toByteArray(), "utf-8"); //outSteam.toByteArray()转换成字节数组
            //将xml字符串转换成Map结构
            Map<String, String> map = WXPayUtil.xmlToMap(result);

            //获取自定义参数
            String attach = map.get("attach");
            Map<String,String> attachMap=JSON.parseObject(attach,Map.class);
            rabbitTemplate.convertAndSend(attachMap.get("exchange"),attachMap.get("routingkey"), JSON.toJSONString(map));
//            //发送支付结果给MQ
//            rabbitTemplate.convertAndSend("exchange.order","queue.order", JSON.toJSONString(map));

            //响应数据设置
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        } catch (Exception e) {
            e.printStackTrace();
            //记录错误日志
        }
        return null;
    }

    /***
     * 创建二维码
     * 普通订单：
     *      exchange:exchange.order
     *      routingkey:queue.order
     * 秒杀订单：
     *      exchange:exchange.seckillorder
     *      routingkey:queue.seckillorder
     *
     *    exchange+routingkey  -->JSON -->attach
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> map){
        Map<String,String> resultMap = null;
        try {
            resultMap = weixinPayService.createNative(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /***
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String out_trade_no){
        Map<String,String> resultMap = weixinPayService.queryPayStatus(out_trade_no);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }
}
