package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues="${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    private SeckillOrderService seckillOrderService;

    @RabbitHandler
    public void getMessage(String message){
//        System.out.println(message);
        try {
            Map<String, String> resultMap = WXPayUtil.xmlToMap(message);

            //return_code ->通信标识 -》SUCCESS
            String return_code = resultMap.get("return_code");
            //out_trade_no->订单号
            String out_trade_no = resultMap.get("out_trade_no");
            //自定义数据
            String attach = resultMap.get("attach");
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            if (return_code.equals("SUCCESS")){
                //result_code -->业务结果--SUCCESS -》该订单状态
                String result_code = resultMap.get("result_code");
                if (result_code.equals("SUCCESS")){
                    //改订单状态
                    seckillOrderService.updatePayStatus(attachMap.get("username"),resultMap.get("transcation_id"),resultMap.get("time_end"));
                    //清理用户信息
                }else {
                    //               FAIL -》删除订单【真实工作中存入到mysql】 --》回滚库存
                    seckillOrderService.closeOrder(attachMap.get("username"));
                }


            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //将支付信息转换成map



    }
}
