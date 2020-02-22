package com.changgou.seckill.service;

public interface SecKillOrderService {
    //秒杀下订单的方法
    boolean add(Long id, String time, String username);
}
