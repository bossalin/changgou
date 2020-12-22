package com.itheima.controller;

import com.itheima.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @author wzq on 2020/12/2.
 *
 * Thymeleaf模板影响
 */
@Controller
@RequestMapping(value = "/test")
public class TestController {

    /**
     * 基本案例
     * @return
     */
    @GetMapping(value = "/hello")
    public String hello(Model model){
        model.addAttribute("message","hello thymeleaf");

        User user1=new User(1,"张三1","深圳");
        User user2=new User(2,"张三2","北京");
        User user3=new User(3,"张三3","上海");
        List<User> list=new ArrayList<User>();
        list.add(user1);
        list.add(user2);
        list.add(user3);
        model.addAttribute("users",list);

        //Map定义
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("No","123");
        dataMap.put("address","深圳");
        model.addAttribute("dataMap",dataMap);

        //存储一个数组
        String[] names = {"张三","李四","王五"};
        model.addAttribute("names",names);


        //日期
        model.addAttribute("now",new Date());

        //if条件
        model.addAttribute("age",22);
        model.addAttribute("hello","hello");
        model.addAttribute("class1","aaa");
        model.addAttribute("class2","bbb");


        return "demo1";
    }
}
