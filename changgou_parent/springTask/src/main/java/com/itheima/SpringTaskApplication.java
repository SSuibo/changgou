package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling   //开启定时任务调度
public class SpringTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringTaskApplication.class, args);
    }
}
