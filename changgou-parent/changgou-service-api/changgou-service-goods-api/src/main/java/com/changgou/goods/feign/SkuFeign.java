package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author wzq on 2020/11/26.
 */

@FeignClient(name="goods")   //调用的服务名
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /***
     * 商品信息递减
     * Map<key,value>  key:要递减的商品ID
     *                 value:要递减的数量
     * @return
     */
    @GetMapping("/decr/count")
    public Result decrCount(Map<String,Integer> decrMap);

    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable Long id);

    /***
     * 根据审核状态查询Sku
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(value = "status") String status);

    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 根据条件搜索的SKU的列表
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);
}