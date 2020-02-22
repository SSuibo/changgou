package com.changgou.order.listener;

import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.changgou.order.listener
 * @ClassName: OrderTrackListener
 * @Author: suibo
 * @Date: 2020/2/9 21:22
 * @Description: //TODO
 */
@Component
public class OrderTackListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "order_tack")
    public void autoTrack(String message){
        System.out.println("收到自动确认收货的消息:" + message);
        orderService.autoTack();    //自动确认收货
    }
}
