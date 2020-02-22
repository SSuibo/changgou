package com.changgou.seckill.feign;

import com.changgou.entity.Result;
import com.changgou.seckill.pojo.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @PackageName: com.changgou.seckill.feign
 * @ClassName: SeckillFeign
 * @Author: suibo
 * @Date: 2020/2/11 19:30
 * @Description: //TODO
 */
@FeignClient(name = "seckill")
public interface SeckillFeign {

    @RequestMapping("/seckill/list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time);
}
