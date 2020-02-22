package com.changgou.user.feign;

import com.changgou.entity.Result;
import com.changgou.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @PackageName: com.changgou.user.feign
 * @ClassName: UserFeign
 * @Author: suibo
 * @Date: 2020/1/13 16:56
 * @Description: //TODO
 */
@FeignClient(name = "user")     //必须要指定对应的微服务的名称
public interface UserFeign {

    @GetMapping("/user/load/{username}")
    public User findUserInfo(@PathVariable("username") String username);

    @GetMapping("/user/points/add")
    public Result addPoints(@RequestParam("points") Integer points);
}
