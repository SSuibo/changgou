package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @PackageName: com.changgou.order.controller
 * @ClassName: CartController
 * @Author: suibo
 * @Date: 2020/1/13 20:37
 * @Description: //TODO
 */
@RestController
@CrossOrigin    //跨域
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;
    //添加购物车
    @GetMapping("/add")
    public Result add(@RequestParam("skuId") String skuId,@RequestParam("num") Integer num){
        //String username = "itcast";
        String username = tokenDecode.getUserInfo().get("username");
        cartService.add(skuId,num,username);
        return new Result(true, StatusCode.OK,"加入购物车成功");
    }

    //根据用户名查询购物车列表
    @GetMapping("/list")
    public Map list(){
        //String username = "itcast";
        String username = tokenDecode.getUserInfo().get("username");
        Map map = cartService.list(username);
        return map;
    }
}
