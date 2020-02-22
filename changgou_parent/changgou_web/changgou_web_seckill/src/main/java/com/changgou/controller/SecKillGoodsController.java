package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.seckill.feign.SeckillFeign;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @PackageName: com.changgou.controller
 * @ClassName: SecKillGoodsController
 * @Author: suibo
 * @Date: 2020/2/10 21:08
 * @Description: //TODO
 */
@Controller
@RequestMapping("/wseckillgoods")
public class SecKillGoodsController {

    @Autowired
    private SeckillFeign seckillFeign;

    @ResponseBody
    @RequestMapping("/timeMenus")
    public List<String> dateMenus(){
        List<Date> dateMenus = DateUtil.getDateMenus();
        List<String> result = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Date dateMenu : dateMenus) {
            String format = simpleDateFormat.format(dateMenu);
            result.add(format);
        }
        return result;
    }

    @RequestMapping("/toIndex")
    public String toIndex(){
        return "seckill-index";
    }

    @RequestMapping("/list")
    @ResponseBody
    public Result<List<SeckillGoods>> list(String time){
        Result<List<SeckillGoods>> listResult = seckillFeign.list(DateUtil.formatStr(time));
        return listResult;
    }
}
