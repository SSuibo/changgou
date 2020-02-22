package com.changgou.user.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @PackageName: com.changgou.order.config
 * @ClassName: RabbitMqConfig
 * @Author: suibo
 * @Date: 2020/1/23 12:42
 * @Description: //基于rabbitmq实现分布式事务
 */
@Configuration
public class RabbitMqConfig {

    //路由模式
    //定义交换机
    public static final String EX_BUYING_ADDPOINTUSER = "ex_buying_addpointuser";

    //定义消息队列(添加积分的消息队列)
    public static final String CG_BUYING_ADDPOINT = "cg_buying_addpoint";

    //定义完成添加积分的消息队列
    public static final String CG_BUYING_FINISHADDPOINT = "cg_buying_finishaddpoint";

    //定义添加积分的路由key
    public static final String CG_BUYING_ADDPOINT_key = "addpoint";

    //定义完成添加积分的路由key
    public static final String CG_BUYING_FINISHADDPOINT_key = "finishaddpoint";

    /**
     * 配置交换机
     */
    @Bean(EX_BUYING_ADDPOINTUSER)
    public Exchange EX_BUYING_ADDPOINTUSER(){
        return ExchangeBuilder.directExchange(EX_BUYING_ADDPOINTUSER).durable(true).build();
    }

    /**
     * 配置队列
     */
    @Bean(CG_BUYING_ADDPOINT)
    public Queue CG_BUYING_ADDPOINT(){
        return new Queue(CG_BUYING_ADDPOINT);
    }

    /**
     * 配置队列
     */
    @Bean(CG_BUYING_FINISHADDPOINT)
    public Queue CG_BUYING_FINISHADDPOINT(){
        return new Queue(CG_BUYING_FINISHADDPOINT);
    }

    /**
     * 指定交换机和完成添加积分的消息队列之间的绑定关系
     */
    @Bean
    public Binding BINDING_QUEUE_FINISHADDPOINT(@Qualifier(CG_BUYING_FINISHADDPOINT) Queue queue,
                                                @Qualifier(EX_BUYING_ADDPOINTUSER)Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(CG_BUYING_FINISHADDPOINT_key).noargs();
    }

    /**
     * 指定交换机和添加积分的消息队列之间的绑定关系
     */
    @Bean
    public Binding BINDING_QUEUE_ADDPOINT(@Qualifier(CG_BUYING_ADDPOINT) Queue queue,
                                                @Qualifier(EX_BUYING_ADDPOINTUSER)Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(CG_BUYING_ADDPOINT_key).noargs();
    }

}
