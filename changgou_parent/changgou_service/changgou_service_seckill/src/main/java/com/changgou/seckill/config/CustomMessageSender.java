package com.changgou.seckill.config;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @PackageName: com.changgou.seckill.config
 * @ClassName: CustomMessageSender
 * @Author: suibo
 * @Date: 2020/2/11 23:02
 * @Description: //TODO
 */
@Component
public class CustomMessageSender implements RabbitTemplate.ConfirmCallback {

    static final Logger log = LoggerFactory.getLogger(CustomMessageSender.class);

    private static final String MESSAGE_CONFIRM_ = "message_confirm_";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public CustomMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            //返回成功的通知,删除redis中的相关数据
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM_ + correlationData.getId());
        } else {
            //返回失败的通知
            Map<String,String> map = redisTemplate.opsForHash().entries(MESSAGE_CONFIRM_ + correlationData.getId());
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String sendMessage = map.get("sendMessage");

            //重复发送
            rabbitTemplate.convertAndSend(exchange,routingKey, JSON.toJSONString(sendMessage));
        }
    }

    //自定义发送方法
    public void sendMessage(String exchange,String routingKey,String message){
        //设置消息唯一标识,并存入缓存
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(correlationData.getId(),message);
        //本次发送的相关信息存入缓存
        Map<String,String> map = new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("sendMessage",message);

        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM_+correlationData.getId(),map);

        //携带唯一标识发送消息
        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);
    }
}
