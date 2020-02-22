package com.changgou.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @PackageName: com.changgou.config
 * @ClassName: RabbitMqConfig
 * @Author: suibo
 * @Date: 2020/2/9 21:12
 * @Description: //TODO
 */
@Configuration
public class RabbitMqConfig {


    public static final String ORDER_TACK = "order_tack";

    @Bean
    public Queue queue(){
        return new Queue(ORDER_TACK);
    }
}
