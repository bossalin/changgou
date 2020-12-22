package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SkuFeign skuFeign;

    @Autowired
    SpuFeign spuFeign;

    /***
     * 加入购物车
     * @param num
     * @param id
     */
    @Override
    public void add(Integer num, Long id ,String username) {



    //查询商品的详情
        //1)查询sku
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        //2）查询spu
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();
        OrderItem orderItem = createOrderItem(num, id, sku, spu);

        //当添加购物车数量<=0的时候，需要一处该商品信息
        if (num<0){
            //移除购物车该商品
            redisTemplate.boundHashOps("Cart_"+username).delete(id);

            //如果此时购物车为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size==null || size<=0){
                redisTemplate.boundHashOps("Cart_" + username).delete();
            }
        }else {
            //将购物车数据出入到redis namespace->username
            // boundHashOps("命名空间")
            redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
        }

    }

    @Override
    public List<OrderItem> list(String username) {
        //redisTemplate.boundHashOps("Cart_"+username).values();获取指定命名空间下所有数据
        List values = redisTemplate.boundHashOps("Cart_" + username).values();
        return values;
    }

    /***
     * 创建一个OrderItem对象
     * @param num
     * @param id
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //将加入购物车的商品信息封装成OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSkuId(id);
        orderItem.setSpuId(spu.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(sku.getNum());
        orderItem.setMoney(num * sku.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
