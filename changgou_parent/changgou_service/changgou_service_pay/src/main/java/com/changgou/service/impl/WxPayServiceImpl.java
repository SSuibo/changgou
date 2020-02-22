package com.changgou.service.impl;

import com.changgou.service.WxPayService;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @PackageName: com.changgou.service.impl
 * @ClassName: WxPayServiceImpl
 * @Author: suibo
 * @Date: 2020/2/6 21:54
 * @Description: //TODO
 */
@Service
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private WXPay wxPay;

    @Value("${wxpay.notify_url}")
    private String notifyUrl;


    //微信下单
    @Override
    public Map nativePay(String orderId, Integer money) {
        try {
            //1.封装请求参数
            Map map = new HashMap();
            map.put("body","畅购");   //商品描述
            map.put("out_trade_no",orderId);    //订单号
            //金额
            BigDecimal payMoney = new BigDecimal("0.01");
            BigDecimal fen = payMoney.multiply(new BigDecimal("100"));//1.00
            fen = fen.setScale(0,BigDecimal.ROUND_UP);  //向上取整1
            map.put("total_fee",String.valueOf(fen));
            map.put("spbill_create_ip","127.0.0.1");    //终端ip
            map.put("notify_url",notifyUrl);   //回调地址
            map.put("trade_type","NATIVE"); //交易类型
            //调用微信统一下单接口方法
            Map mapResult = wxPay.unifiedOrder(map);
            return mapResult;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    //基于微信查询订单
    @Override
    public Map queryOrder(String OrderId) {
        Map map = new HashMap();
        map.put("out_trade_no",OrderId);
        try {
            Map result = wxPay.orderQuery(map);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //关闭订单
    @Override
    public Map closeOrder(String orderId) {
        Map map = new HashMap();
        map.put("out_trade_no",orderId);
        try {
            return wxPay.closeOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
