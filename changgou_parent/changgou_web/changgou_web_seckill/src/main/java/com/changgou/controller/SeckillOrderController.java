package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.seckill.feign.SeckillOrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PackageName: com.changgou.controller
 * @ClassName: wseckillorder
 * @Author: suibo
 * @Date: 2020/2/11 20:20
 * @Description: //TODO
 */
@RestController
@CrossOrigin
@RequestMapping("/wseckillorder")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderFeign seckillOrderFeign;

    @RequestMapping("/add")
    public Result add(@RequestParam("time") String time,@RequestParam("id") Long id){
        Result result = seckillOrderFeign.add(time, id);
        return result;
    }
}
