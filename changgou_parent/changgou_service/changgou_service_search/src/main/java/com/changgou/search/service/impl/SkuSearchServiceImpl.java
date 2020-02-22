package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.ESManagerMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @PackageName: com.changgou.search.service.impl
 * @ClassName: SkuSearchServiceImpl
 * @Author: suibo
 * @Date: 2020/1/5 20:35
 * @Description: //TODO
 */
@Service
public class SkuSearchServiceImpl implements ESManagerService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private ESManagerMapper esManagerMapper;

    @Override
    public void creatESIndeces(){
        //创建索引库
        elasticsearchTemplate.createIndex(SkuInfo.class);
        //添加映射关系
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    //导入所有sku数据到索引库
    @Override
    public void importAll() {
        //通过feign远程调用微服务
        List<Sku> skuList = skuFeign.findSkuListBySpuId("all");
        if(skuList==null || skuList.size()==0){
            throw new RuntimeException("没有查询到任何sku的信息,无法添加到索引库");
        }
        //将skuList转换为json
        String skuString = JSON.toJSONString(skuList);
        //将skuString转换为skuInfo,因为最后添加到索引库的是SkuInfo
        List<SkuInfo> skuInfoList = JSON.parseArray(skuString, SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            //将规格信息转换为map
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }

        //存储
        esManagerMapper.saveAll(skuInfoList);
    }

    //根据spuId导入sku数据到索引库
    @Override
    public void importDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if(skuList==null || skuList.size()<=0){
            throw new RuntimeException("没有查询到任何sku的信息,无法添加到索引库");
        }
        //将skuList转成json字符串
        String skuString = JSON.toJSONString(skuList);
        //将skuString转为skuInfo
        List<SkuInfo> skuInfoList = JSON.parseArray(skuString, SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //存储
        esManagerMapper.saveAll(skuInfoList);
    }

    //根据spuId从索引库删除sku信息
    @Override
    public void delDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if(skuList==null && skuList.size()<=0){
            throw new RuntimeException("索引库无此数据,无法删除");
        }
        for (Sku sku : skuList) {
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }
    }
}
