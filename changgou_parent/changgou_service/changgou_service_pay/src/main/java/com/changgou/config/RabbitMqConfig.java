package com.changgou.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @PackageName: com.changgou.config
 * @ClassName: RabbitMqConfig
 * @Author: suibo
 * @Date: 2020/2/7 19:24
 * @Description: //TODO
 */
@Configuration
public class RabbitMqConfig {

    private static final String ORDER_PAY = "order_pay";

    @Bean
    public Queue queue(){
        return new Queue(ORDER_PAY);
    }

}
