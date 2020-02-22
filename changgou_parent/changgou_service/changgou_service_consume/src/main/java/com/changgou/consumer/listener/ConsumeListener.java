package com.changgou.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.consumer.config.RabbitMQConfig;
import com.changgou.consumer.service.SeckillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @PackageName: com.changgou.consumer.listener
 * @ClassName: ConsumeListener
 * @Author: suibo
 * @Date: 2020/2/12 19:50
 * @Description: //TODO
 */
@Component
public class ConsumeListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_KEY)
    public void receiveSeckillOrderMessage(Message message, Channel channel){

        //设置预抓取总数
        try {
            channel.basicQos(300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //转换消息
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(), SeckillOrder.class);
        int rows = seckillOrderService.createOrder(seckillOrder);//返回值是数据库影响行数

        if(rows>0){
            //成功添加秒杀订单,返回成功通知
            try {
                /**
                 * 第二个参数:
                 *      true:所有消费者都会接受此队列的消息
                 *      false:只有当前消费者接受此消息
                 */
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            //返回失败的通知
            try {
                /**
                 * 第二个参数:true:所有消费者都会拒绝此消息;false:只有当前消费者会拒绝此消息
                 * 第三个参数:true:如果设置死信队列,消息会进入死信队列.false:消息会回到原有队列中,默认是头部
                 */
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
