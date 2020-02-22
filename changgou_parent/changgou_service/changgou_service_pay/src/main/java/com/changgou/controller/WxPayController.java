package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import com.github.wxpay.sdk.WXPayXmlUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @PackageName: com.changgou.controller
 * @ClassName: WxPayController
 * @Author: suibo
 * @Date: 2020/2/6 21:43
 * @Description: //TODO
 */
@RestController
@RequestMapping("/wxpay")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId,@RequestParam("money") Integer money){
        Map map = wxPayService.nativePay(orderId, money);
        return new Result(true, StatusCode.OK,"",map);
    }

    @RequestMapping("/notify")
    public void notifyLogic(HttpServletRequest request, HttpServletResponse response){
        System.out.println("支付成功回调...");
        try {
            //微信以流的形式(二进制)给畅购系统一个支付成功或者失败的响应结果,所以这块要将二进制流转换为字符串
            ServletInputStream sis = request.getInputStream();
            String xml = IOUtils.toString(sis);
            System.out.println(xml);
            //将xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(xml);

            if("SUCCESS".equals(map.get("result_code"))){
                //返回的结果是成功
                Map result = wxPayService.queryOrder(map.get("out_trade_no"));
                System.out.println("查询订单返回结果:" + result);
                if("SUCCESS".equals(map.get("result_code"))){
                    //查询到了相关订单
                    Map m = new HashMap();
                    //订单id和交易流水号
                    m.put("orderId",result.get("out_trade_no"));
                    m.put("transactionId",result.get("transaction_id"));

                    rabbitTemplate.convertAndSend("","order_pay", JSON.toJSONString(m));

                    //发送消息,完成双向通信
                    rabbitTemplate.convertAndSend("paynotify","",result.get("out_trade_no"));
                }
            }else {
                System.out.println(map.get("err_code_des"));
            }

            //给微信一个接受成功的响应
            response.setContentType("text/xml");
            String data = "<xml>\n" +
                    "\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
            response.getWriter().write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable("orderId") String orderId){
        Map map = wxPayService.closeOrder(orderId);
        return new Result(true,StatusCode.OK,"关闭订单成功",map);
    }

    @GetMapping("/query/{orderId}")
    public Result queryOrder(@PathVariable("orderId") String orderId){
        Map map = wxPayService.queryOrder(orderId);
        return new Result(true,StatusCode.OK,"",map);
    }
}
