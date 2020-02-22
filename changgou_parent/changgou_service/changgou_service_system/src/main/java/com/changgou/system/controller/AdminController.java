package com.changgou.system.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.system.service.AdminService;
import com.changgou.system.pojo.Admin;
import com.changgou.system.util.JwtUtil;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private AdminService adminService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Admin> adminList = adminService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",adminList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id){
        Admin admin = adminService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",admin);
    }


    /***
     * 新增数据
     * @param admin
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param admin
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Admin admin,@PathVariable Integer id){
        admin.setId(id);
        adminService.update(admin);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Integer id){
        adminService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Admin> list = adminService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Admin> pageList = adminService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }

    /**
     * 用户登录验证,将明文密码与数据库中加密后的密码进行比对
     */
    @PostMapping("/login")
    public Result login(@RequestBody Admin admin){
        Boolean login = adminService.login(admin);
        if(login){
            //登录成功
            /**
             * 添加token,用户登录成功之后,需要携带着这个token才能访问其他微服务
             * jwt其实就是一个字符串,包含三部分信息,头部,载荷和签名,将响应的数据以map集合的形式返回
             */
            Map<String,String> info = new HashMap<>();
            //显示当前登录者是谁
            info.put("username",admin.getLoginName());
            //使用工具类创建JWT
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(),admin.getLoginName(),null);
            info.put("token",token);
            return new Result(true,StatusCode.OK,"登陆成功",info);
        }else {
            //登录失败
            return new Result(false,StatusCode.LOGINERROR,"用户名或密码错误");
        }
    }
}