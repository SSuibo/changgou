package com.changgou.order.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @PackageName: com.changgou.order.feign
 * @ClassName: CartFeign
 * @Author: suibo
 * @Date: 2020/1/14 15:25
 * @Description: //购物车页面渲染feign接口
 */
@FeignClient(name = "order")
public interface CartFeign {

    @GetMapping("/cart/add")
    public Result add(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num);

    @GetMapping("/cart/list")
    public Map list();
}
