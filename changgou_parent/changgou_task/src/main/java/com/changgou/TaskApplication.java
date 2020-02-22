package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @PackageName: com.changgou
 * @ClassName: TaskApplication
 * @Author: suibo
 * @Date: 2020/2/9 21:07
 * @Description: //TODO
 */
@SpringBootApplication
@EnableScheduling
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class,args);
    }
}
