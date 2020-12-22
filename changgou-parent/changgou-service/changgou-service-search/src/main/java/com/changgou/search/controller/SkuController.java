package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wzq on 2020/11/29.
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @PostMapping
    public Map search(@RequestBody(required = false) Map searchMap){
        return  skuService.search(searchMap);
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search_get(@RequestParam(required = false) Map<String,String> searchMap){  //允许为空  参数?keywords=""
//        return  skuService.search(searchMap);
        return skuService.search_groud(searchMap);
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping("/brandlist")
    public Map search_brandlist(@RequestParam(required = false) Map<String,String> searchMap){  //允许为空  参数?keywords=""
        return  skuService.search_new(searchMap);
    }
    /**
     * 导入数据
     * @return
     */
    @GetMapping("/import")
    public Result search(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }
}