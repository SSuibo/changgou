package com.changgou.search.listener;

import com.changgou.search.config.RabbitMqConfig;
import com.changgou.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.changgou.search.listener
 * @ClassName: GoodsDownListener
 * @Author: suibo
 * @Date: 2020/1/5 23:26
 * @Description: //商品下线的监听类
 */
@Component
public class GoodsDownListener {

    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMqConfig.SEARCH_DELETE_QUEUE)
    public void delDataBySpuId(String spuId){
        System.out.println("监听到的商品下线的队列的消息是: " + spuId);
        esManagerService.delDataBySpuId(spuId);
    }
}
