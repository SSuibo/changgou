package com.changgou.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @PackageName: com.changgou.seckill.config
 * @ClassName: RabbitMQConfig
 * @Author: suibo
 * @Date: 2020/2/11 22:58
 * @Description: //TODO
 */
@Configuration
public class RabbitMQConfig {

    public static final String SECKILL_ORDER_KEY = "seckill_order";

    @Bean
    public Queue queue(){
        //开启持久化
        return new Queue(SECKILL_ORDER_KEY,true);
    }
}
