package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


/**
 * @author wzq on 2020/11/16.
 * Dao使用通用mapper，只需要继承一个接口
 *
 *  增加数据，调用Mapper.insert()
 *  增加数据，调用Mapper.insertSelective()
 *
 *  修改数据，调用Mapper.update(T)
 *              Mapper.updatePrimayKey（ID）
 *
 *  查询数据：根据ID查询：Mapper.selectByPrimaryKey(ID)
 *                      Mapper.select(T)
 *
 *
 */
public interface BrandMapper extends Mapper<Brand> {
    /***
     * 查询分类对应的品牌集合
     */
    @Select("SELECT tb.* FROM tb_category_brand tcb,tb_brand tb WHERE tcb.category_id=#{categoryid} AND tb.id=tcb.brand_id")
    List<Brand> findByCategory(Integer categoryid);
}
