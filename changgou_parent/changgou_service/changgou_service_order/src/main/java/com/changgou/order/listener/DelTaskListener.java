package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.pojo.Task;
import com.changgou.order.service.TaskService;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @PackageName: com.changgou.order.listener
 * @ClassName: DelTaskListener
 * @Author: suibo
 * @Date: 2020/1/28 19:16
 * @Description: //TODO
 */
@Component
public class DelTaskListener {

    @Autowired
    private TaskService taskService;
    @RabbitListener(queues = RabbitMqConfig.CG_BUYING_FINISHADDPOINT)
    public void receiveDelTaskMessage(String message){
        System.out.println("订单服务接收到了删除任务操作的消息");

        Task task = JSON.parseObject(message, Task.class);

        taskService.delTask(task);
    }
}
