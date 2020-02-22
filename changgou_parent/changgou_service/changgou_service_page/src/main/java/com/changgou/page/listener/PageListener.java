package com.changgou.page.listener;

import com.changgou.page.config.RabbitMqConfig;
import com.changgou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.changgou.page.listener
 * @ClassName: PageListener
 * @Author: suibo
 * @Date: 2020/1/9 21:29
 * @Description: //商品上架生成详情页静态页面的监听类
 */
@Component
public class PageListener {

    @Autowired
    private PageService pageService;

    @RabbitListener(queues = RabbitMqConfig.CREATE_PAGE_QUEUE)
    public void receiveMessage(String spuId){
        System.out.println("监听到的商品上架的消息的id是: " + spuId);
        pageService.generateItemPage(spuId);
    }
}
