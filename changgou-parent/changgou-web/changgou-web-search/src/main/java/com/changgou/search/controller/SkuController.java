package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author wzq on 2020/12/3.
 */
@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){
        //调用changgou-service-search微服务
//        Map resultMap = skuFeign.search(searchMap);
        Map resultMap = skuFeign.search_brandlist(searchMap);
        model.addAttribute("result",resultMap);
        model.addAttribute("searchMap",searchMap);
        //获取上次请求的url
        String[] urls=geturl(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);
        //分页计算
        Page<SkuInfo> page=new Page<SkuInfo>(
                Long.parseLong(resultMap.get("total").toString()),  //总记录数
                Integer.parseInt(resultMap.get("pageNum").toString()),  //当前页数
                Integer.parseInt(resultMap.get("pageSize").toString())  //每页显示的条数
        );
        model.addAttribute("page",page);
        return "search";
    }


    /**
     * 拼接组装用户请求的url
     * 获取用户每次请求的地址
     * 页面需要在这次请求的地址上添加额外的搜索条件
     * http://localhost:18087/search/list
     * http://localhost:18087/search/list?brand=华为
     * 。。。
     */
    public String[] geturl(Map<String,String> searchMap){
        String[] s=new String[2];
        String url="/search/list";//初始化地址
        String sorturl="/search/list"; //排序地址
        if (searchMap!=null && searchMap.size()>0){
            url+="?";
            sorturl+="?";
            for (Map.Entry<String,String> entry:searchMap.entrySet()) {
                if(entry.getKey().equalsIgnoreCase("pageNum")){
                    continue;
                }
                //key是搜索的对象
                String key = entry.getKey();
                //value是搜索的值
                String value = entry.getValue();
                url+=key+"="+value+"&";

                //排序参数，跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")){
                    continue;
                }

                sorturl+=key+"="+value+"&";
            }
            //去掉最后一个&
            url=url.substring(0,url.length()-1);
            sorturl=sorturl.substring(0,sorturl.length()-1);
            s[0]=url;
            s[1]=sorturl;
        }
        return s;
    }
}
