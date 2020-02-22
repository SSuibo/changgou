package com.changgou.search.service;

import java.util.Map;

public interface SearchService {
    //根据搜索框输入的关键字从索引库搜索商品(根据name域)
    Map<String,Object> search(Map<String,String> searchMap);
}
