package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.WxPayFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.jws.WebParam;
import java.util.Map;

/**
 * @PackageName: com.changgou.controller
 * @ClassName: PayController
 * @Author: suibo
 * @Date: 2020/2/6 22:25
 * @Description: //TODO
 */
@Controller
@RequestMapping("/wxpay")
public class PayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private WxPayFeign wxPayFeign;

    @GetMapping
    public String wxPay(String orderId, Model model){
        Result<Order> orderResult = orderFeign.findById(orderId);
        if(orderResult.getData()==null){
            return "fail";  //错误页面
        }
        Order order = orderResult.getData();
        //判断支付状态
        if(!"0".equals(order.getPayStatus())){//如果不是未支付订单
            return "fail";
        }

        Result payResult = wxPayFeign.nativePay(orderId, order.getTotalMoney());
        if(payResult.getData()==null){
            return "fail";
        }

        Map payMap = (Map) payResult.getData();
        payMap.put("payMoney",order.getTotalMoney());
        payMap.put("orderId",orderId);
        model.addAllAttributes(payMap);
        return "wxpay";

    }


    /**
     * 支付成功页面的跳转
     */
    @RequestMapping("/toPaySuccess")
    public String toPaySuccess(Integer payMoney, Model model){
        model.addAttribute("payMoney",payMoney);
        return "paysuccess";
    }
}
