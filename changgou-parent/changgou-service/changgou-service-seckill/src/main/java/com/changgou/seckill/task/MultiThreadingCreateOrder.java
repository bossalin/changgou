package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;
    /***
     * 多线程下单操作
     * @Async:该方法会异步执行，底层还是多线程执行
     */
    @Async
    public void createOrder(){
        try {
            //从Redis对类中获取用户排队信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            if(seckillStatus==null){
                return;
            }

            //先到SeckillGoodsCountList队列中获取该商品的一个信息，如果能获取到，则可以下单
            Object o = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            //如果不能获取到该商品的队列信息，则表示没有库存，清理排队信息
            if (o==null){
                //则表示没有库存，清理排队信息
                clearUserQueue(seckillStatus.getUsername());
                return;
            }


//            String username="wu";
            String username=seckillStatus.getUsername();
//            Long id=1131814839702917120L;
            Long id=seckillStatus.getGoodsId();

//            String time ="2020122018";
            String time=seckillStatus.getTime();
            System.out.println("time:"+time);
            System.out.println("开始下单");
            Thread.sleep(10000);
            //获取商品数据
            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            System.out.println("goods:"+goods);
            //如果没有库存，则直接抛出异常
            if(goods==null || goods.getStockCount()<=0){
                throw new RuntimeException("已售罄!");
            }
            //如果有库存，则创建秒杀商品订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);
            seckillOrder.setMoney(goods.getCostPrice());
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");

            //将秒杀订单存入到Redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username,seckillOrder);

            //库存减少
            goods.setStockCount(goods.getStockCount()-1);
            Thread.sleep(10000);
            //观察多线程库存问题
            System.out.println(Thread.currentThread().getName() + "操作后剩余库存=" + goods.getStockCount());
            //获取该商品队列数量
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).size();



            //判断当前商品是否还有库存
//            if(goods.getStockCount()<=0){
            if (size<=0){
                goods.setStockCount(size.intValue());
                //并且将商品数据同步到MySQL中
                seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                //如果没有库存,则清空Redis缓存中该商品
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            }else{
                //如果有库存，则直数据重置到Reids中
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id,goods);
            }
            System.out.println("下单成功");


            seckillStatus.setMoney(Float.valueOf(goods.getCostPrice()));
            seckillStatus.setStatus(2); //待付款
            redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 清理用户排队抢单信息
     * @param username
     */
    public void clearUserQueue(String username){
        //排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //排队信息清理掉
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }
}