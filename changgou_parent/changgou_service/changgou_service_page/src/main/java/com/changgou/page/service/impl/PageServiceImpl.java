package com.changgou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PackageName: com.changgou.page.service.impl
 * @ClassName: PageServiceImpl
 * @Author: suibo
 * @Date: 2020/1/9 19:49
 * @Description: //TODO
 */
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagepath}")
    private String pagepath;

    @Override
    public void generateItemPage(String spuId) {
        //获取context对象,用于存放商品数据
        Context context = new Context();

        Map<String, Object> itemData = this.findItemData(spuId);
        context.setVariables(itemData);
        //获取商品详情页生成的位置信息
        File dir = new File(pagepath);
        //如果文件不存在,就把他创建出来
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir + "/" + spuId + ".html");

        //定义生成详情页所需的流
        Writer out = null;
        try {
            out = new PrintWriter(file);
            templateEngine.process("item", context, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Object> findItemData(String spuId) {
        Map<String, Object> resultMap = new HashMap<>();

        //封装图片数据(spu的信息)
        Spu spu = spuFeign.findById(spuId).getData();
        if (spu != null) {
            if (!StringUtils.isEmpty(spu.getImages())) {
                resultMap.put("spu",spu);
                resultMap.put("imageList", spu.getImages().split(","));
            }
        }
        //封装分类数据
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1", category1);
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        resultMap.put("category2", category2);
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category3", category3);

        //获取sku集合信息
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList", skuList);

        //获取规格信息
        resultMap.put("specificationList", JSON.parseObject(spu.getSpecItems(), Map.class));


        return resultMap;
    }
}
