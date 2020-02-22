package com.itheima.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @PackageName: com.itheima.task
 * @ClassName: MyTask
 * @Author: suibo
 * @Date: 2020/2/3 11:51
 * @Description: //TODO
 */
@Component  //交由spring来管理
public class MyTask {

    //cron表达式总共有7个域:分别是秒分时日月周年,springtask不支持年这个域
    @Scheduled(cron = "0/2 * * * * ?")  //0/2表示从0秒开始,每隔两秒执行一次,*代表任意,?代表放弃这个域
    //必须由public void开头,方法名任意
    public void myMethod(){
        System.out.println("定时任务调度框架正在执行:" + new Date());
    }
}
