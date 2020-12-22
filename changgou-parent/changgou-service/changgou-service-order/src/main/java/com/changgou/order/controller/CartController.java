package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    /***
     * 加入购物车
     * 1.加入购物车的数量
     * 2.商品的ID
     */
    @GetMapping("/add")
    public Result add(@RequestParam(value = "num") Integer num,@RequestParam(value = "id")Long id){
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo);
        String username=userInfo.get("username");
        cartService.add(num,id,username);
        return new Result(true, StatusCode.OK,"加入购物车成功（在redis）");
    }


    /***
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<OrderItem>> list(){
        //用户的令牌信息 ->解析令牌信息 ->username
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo);
        String username=userInfo.get("username");
        List<OrderItem> list = cartService.list(username);
        return new Result<>(true,StatusCode.OK,"查询购物车数据成功",list);
    }
}
