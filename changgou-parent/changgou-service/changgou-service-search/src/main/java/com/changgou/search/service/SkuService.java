package com.changgou.search.service;

import java.util.Map;

/**
 * @author wzq on 2020/11/26.
 */
public interface SkuService {

    /***
     * 分组查询分类集合
     * @param searchMap
     * @return
     */
    Map search_groud(Map<String, String> searchMap);

    /***
     * 搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);

    Map search_new(Map<String, String> searchMap);

    /**
     * 导入数据到索引库中
     */
    void importSku();
}
