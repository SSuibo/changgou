package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMqConfig;
import com.changgou.user.dao.PointLogMapper;
import com.changgou.user.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @PackageName: com.changgou.user.listener
 * @ClassName: AddPointListener
 * @Author: suibo
 * @Date: 2020/1/28 15:04
 * @Description: //TODO
 */
@Component
public class AddPointListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @RabbitListener(queues = RabbitMqConfig.CG_BUYING_ADDPOINT)
    public void recieveAddPoint(String message){
        System.out.println("用户服务接收到了消息");
        //将消息转换为task实体类
        Task task = JSON.parseObject(message, Task.class);
        if(task==null || StringUtils.isEmpty(task.getRequestBody())){
            return;
        }
        //判断redis中有没有任务
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if(value !=null){
            //证明redis中有任务,直接返回
            return;
        }

        //更新用户积分
        int result = userService.updateUserPoints(task);
        System.out.println("用户服务完成了更新用户积分的操作");
        if(result==0){
            return;
        }

        //向订单服务返回通知
        rabbitTemplate.convertAndSend(RabbitMqConfig.EX_BUYING_ADDPOINTUSER,RabbitMqConfig.CG_BUYING_FINISHADDPOINT_key,JSON.toJSONString(task));
        System.out.println("用户服务向完成添加积分队列发送了一条消息");
    }

}
