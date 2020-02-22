package com.changgou.task;

import com.changgou.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @PackageName: com.changgou.task
 * @ClassName: OrderTask
 * @Author: suibo
 * @Date: 2020/2/9 21:15
 * @Description: //TODO
 */
@Component
public class OrderTask {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void autoTake(){
        System.out.println(new Date());
        rabbitTemplate.convertAndSend("", RabbitMqConfig.ORDER_TACK,"===");

    }
}
