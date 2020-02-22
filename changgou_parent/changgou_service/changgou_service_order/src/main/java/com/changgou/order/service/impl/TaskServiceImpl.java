package com.changgou.order.service.impl;

import com.changgou.order.dao.TaskHitsMapper;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import com.changgou.order.pojo.TaskHis;
import com.changgou.order.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @PackageName: com.changgou.order.service.impl
 * @ClassName: TaskServiceImpl
 * @Author: suibo
 * @Date: 2020/1/28 19:19
 * @Description: //TODO
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskHitsMapper taskHitsMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Transactional
    @Override
    public void delTask(Task task) {
        //记录删除的时间
        task.setDeleteTime(new Date());

        Long id = task.getId();
        task.setId(null);

        //bean拷贝,操作的两个实体类里面的属性名必须一致,否则无法进行copy
        TaskHis taskHis = new TaskHis();
        BeanUtils.copyProperties(task,taskHis);

        //记录历史任务数据
        taskHitsMapper.insertSelective(taskHis);
        //删除原有任务数据
        task.setId(id);
        taskMapper.deleteByPrimaryKey(task);
        System.out.println("当前的订单服务完成了添加历史任务并删除原有任务的操作");
    }
}
