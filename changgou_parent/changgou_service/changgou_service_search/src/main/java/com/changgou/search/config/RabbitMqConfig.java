package com.changgou.search.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @PackageName: com.itheima.canal.config
 * @ClassName: RabbitMqConfig
 * @Author: suibo
 * @Date: 2020/1/4 14:13
 * @Description: //RabbitMQ配置类,要实现首页消息的实时更新,用到消息中间件,所以首先要创建消息队列
 */
@Configuration
public class RabbitMqConfig {

    //定义更新首页广告缓存的队列名称
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";

    //定义队列名称
    public static final String SEARCH_ADD_QUEUE = "search_add_queue";

    //定义交换机名称
    public static final String GOODS_UP_EXCHANGE = "goods_up_exchange";

    //定义商品下架的队列
    public static final String SEARCH_DELETE_QUEUE = "search_delete_queue";

    //定义商品下架的交换机
    public static final String GOODS_DOWN_EXCHANGE = "goods_down_exchange";
    //更新首页广告的queue
    @Bean
    public Queue queue(){
        return new Queue(AD_UPDATE_QUEUE);
    }

    @Bean(SEARCH_ADD_QUEUE)
    public Queue SEARCH_ADD_QUEUE(){
        return new Queue(SEARCH_ADD_QUEUE);
    }

    @Bean(GOODS_UP_EXCHANGE)
    public Exchange GOODS_UP_EXCHANGE(){
        return ExchangeBuilder.fanoutExchange(GOODS_UP_EXCHANGE).durable(true).build();
    }

    //绑定交换机和队列
    @Bean
    public Binding binding(@Qualifier(SEARCH_ADD_QUEUE)Queue queue,@Qualifier(GOODS_UP_EXCHANGE)Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    //商品下线的队列
    @Bean(SEARCH_DELETE_QUEUE)
    public Queue SEARCH_DELETE_QUEUE(){
        return new Queue(SEARCH_DELETE_QUEUE);
    }
    //商品下线的交换机
    @Bean(GOODS_DOWN_EXCHANGE)
    public Exchange GOODS_DOWN_EXCHANGE(){
        return ExchangeBuilder.fanoutExchange(GOODS_DOWN_EXCHANGE).durable(true).build();
    }

    //商品下线的交换机和队列的绑定
    @Bean
    public Binding GOODS_DOWN_EXCHANGE_BINDING(@Qualifier(SEARCH_DELETE_QUEUE) Queue queue,@Qualifier(GOODS_DOWN_EXCHANGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }
}
