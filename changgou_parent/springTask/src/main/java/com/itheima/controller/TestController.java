package com.itheima.controller;

import com.itheima.domain.User;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;
import java.util.Map;

/**
 * @PackageName: com.itheima.controller
 * @ClassName: TestController
 * @Author: suibo
 * @Date: 2020/2/3 14:59
 * @Description: //几种接受前端请求参数的形式,那么怎么确定前端用哪种形式来携带参数呢?,根据参数的个数来进行判断
 * 如果是携带一个请求参数,推荐用restful风格
 * 如果是携带两个请求参数,推荐用key=value形式
 * 如果是携带三个请求参数,推荐用json形式来发送请求
 *
 * 后台开发人员如何确定前台用的是哪种形式传递的参数:开发之前会提前写一个约定俗成的接口文档,根据接口文档来确定前端传递参数的形式
 *
 * 那么传递参数有三种形式:
 * 1.第一种是key=value形式
 *      后台接受参数时:可以用简单类型来接受,实体类来接受,map来接受(@RequestParam)
 * 2.json格式传递参数
 *      后台接受参数时:可以用简单类型来接受,可以用map来接受
 * 3.restful风格传递参数
 *      后台接受请求参数时:只能用简单类型来接受参数
 */
@RestController
@RequestMapping("/test")
public class TestController {

    //1.key=value形式传递参数,简单类型接受请求参数
    @PostMapping("/test1")  //localhost:8080/test/test1?username=lisi&age=3
    public void test1(String username,Integer age){
        System.out.println(username);
        System.out.println(age);
    }
    //1.key=value形式传递参数,实体类接受请求参数
    @PostMapping("/test2")  //localhost:8080/test/test1?username=lisi&age=3
    public void test2(User user){
        System.out.println(user.getUsername());
        System.out.println(user.getAge());
    }

    //1.key=value形式传递参数,map接受请求参数
    @PostMapping("/test3")  //localhost:8080/test/test1?username=lisi&age=3
    public void test3(@RequestParam Map map){
        System.out.println(map);
    }

    //2.json形式传递参数,简单类型接受参数
    @PostMapping("/test4")
    public void test4(@RequestBody String user){
        System.out.println(user);
    }

    //2.json形式传递参数,map接受参数
    @PostMapping("/test5")
    public void test5(@RequestBody Map user){
        System.out.println(user);
    }

    //3.restful风格传递请求参数,只能用简单类型来接受,参数是携带在url中的
    @PostMapping("/test6/{username}/{age}")
    public void test6(@PathVariable("username") String username,@PathVariable("age") Integer age){
        System.out.println(username);
        System.out.println(age);
    }
}
