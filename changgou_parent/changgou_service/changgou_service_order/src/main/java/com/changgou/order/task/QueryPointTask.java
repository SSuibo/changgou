package com.changgou.order.task;

import com.alibaba.fastjson.JSON;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @PackageName: com.changgou.order.task
 * @ClassName: QueryPointTask
 * @Author: suibo
 * @Date: 2020/1/28 14:34
 * @Description: //定时任务
 */
@Component
public class QueryPointTask {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Scheduled(cron = "0/2 * * * * ?")      //每隔两秒扫描一次任务表
    public void queryTask(){
        //查询任务表中的所有任务
        List<Task> taskList = taskMapper.selectAll();
        if (!StringUtils.isEmpty(taskList)){
            for (Task task : taskList) {
                //发送消息到rabbitmq
                rabbitTemplate.convertAndSend(task.getMqExchange(),task.getMqRoutingkey(), JSON.toJSONString(task));
                System.out.println("订单服务向添加积分的队列发送了一条消息");
            }
        }

    }
}
