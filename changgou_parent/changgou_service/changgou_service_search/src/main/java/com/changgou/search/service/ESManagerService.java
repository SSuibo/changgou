package com.changgou.search.service;

public interface ESManagerService {

    //创建索引库
    void creatESIndeces();

    //导入所有到es索引库
    void importAll();

    //根据spuId导入对应的skuList到es索引库
    void importDataBySpuId(String spuId);

    //根据spuId从索引库删除sku信息
    void delDataBySpuId(String spuId);
}
