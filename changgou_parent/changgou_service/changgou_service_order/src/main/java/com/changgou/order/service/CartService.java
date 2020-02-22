package com.changgou.order.service;

import java.util.Map;

public interface CartService {

    //添加购物车
    void add(String skuId,Integer num,String username);

    //查询购物车列表
    Map list(String username);
}
