package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PackageName: com.changgou.search.controller
 * @ClassName: ESManagerController
 * @Author: suibo
 * @Date: 2020/1/5 20:59
 * @Description: //TODO
 */
@RestController
@RequestMapping("/manager")
public class ESManagerController {

    @Autowired
    private ESManagerService esManagerService;

    @GetMapping("/create")
    public Result createIndeces(){
        esManagerService.creatESIndeces();
        return new Result(true, StatusCode.OK,"创建索引库成功");
    }

    @GetMapping("/importAll")
    public Result importAll(){
        esManagerService.importAll();
        return new Result(true,StatusCode.OK,"导入所有数据成功");
    }
}
