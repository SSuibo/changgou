package com.changgou.service;

import java.util.Map;

public interface WxPayService {
    Map nativePay(String orderId,Integer money);

    Map queryOrder(String OrderId);

    //关闭订单
    Map closeOrder(String orderId);
}
