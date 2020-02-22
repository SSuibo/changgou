package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    //根据商品分类名称查询品牌列表

    /**
     * select注解用的是Mybatis中类似的注解,删除用delete,添加用insert,修改用update
     * sql语句用的是显式(或隐士)内连接;需求是根据商品分类名称查询品牌列表,如果对应的品牌还没有进行分类的话查出来此品牌就毫无意义
     * 而如果用左外连接的话,即使品牌没有进行分类也照样查出来,显得毫无意义,所以用内连接比较合适
     * @param categoryName
     * @return
     */
    @Select("select b.* from `tb_brand` b\n" +
            "inner join `tb_category_brand` cb on cb.brand_id=b.id\n" +
            "inner join `tb_category` c on cb.category_id=c.id\n" +
            "where c.name=#{categoryName}")
    List<Map> findBrandListByCategroyName(@Param("categoryName") String categoryName);
}
