package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    //根据商品分类名称查询规格列表
    @Select("SELECT DISTINCT s.* FROM `tb_spec` s\n" +
            "INNER JOIN `tb_template` t ON s.`template_id`=t.`id`\n" +
            "INNER JOIN `tb_category` c ON c.`template_id`=t.`id`\n" +
            "WHERE c.`name`=#{categoryName}")
    List<Map> findSpecListByCategoryName(@Param("categoryName") String categoryName);
}
