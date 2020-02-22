package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @PackageName: com.changgou.order.listener
 * @ClassName: OrderPayListener
 * @Author: suibo
 * @Date: 2020/2/7 19:56
 * @Description: //TODO
 */
@Component
@RabbitListener(queues = "order_pay")
public class OrderPayListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void receivePayMessage(String message){
        System.out.println("接收到消息:" + message);
        Map map = JSON.parseObject(message, Map.class);
        orderService.updatePayStatus( (String)map.get("orderId"), (String)map.get("transactionId") );
    }
}
