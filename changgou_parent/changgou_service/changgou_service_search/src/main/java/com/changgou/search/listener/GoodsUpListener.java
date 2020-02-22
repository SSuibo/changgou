package com.changgou.search.listener;

import com.changgou.search.config.RabbitMqConfig;
import com.changgou.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.changgou.search.listener
 * @ClassName: GoodsUpListener
 * @Author: suibo
 * @Date: 2020/1/5 21:13
 * @Description: //TODO
 */
@Component
public class GoodsUpListener {

    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMqConfig.SEARCH_ADD_QUEUE)
    public void receiveMessage(String spuId){

        System.out.println("接受到的消息是: " + spuId);
        esManagerService.importDataBySpuId(spuId);
    }
}
