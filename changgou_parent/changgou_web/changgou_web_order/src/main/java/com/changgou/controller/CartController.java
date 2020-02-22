package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;


/**
 * @PackageName: com.changgou.controller
 * @ClassName: CartController
 * @Author: suibo
 * @Date: 2020/1/14 17:57
 * @Description: //TODO
 */
@Controller
@RequestMapping("/wcart")
public class CartController {

    @Autowired
    private CartFeign cartFeign;

    @GetMapping("/cart")
    private String cart(){
        return "cart";
    }

    //查询购物车列表
    @GetMapping("/list")
    @ResponseBody
    public Result getCart(){
        Map map = cartFeign.list();
        return new Result(true,StatusCode.OK,"查询购物车列表成功",map);
    }

    //添加购物车列表
    @GetMapping("/add")
    @ResponseBody
    public Result add(String skuId,Integer num){
        cartFeign.add(skuId, num);
        //添加完购物车之后,重新查询一下再返回给前台
        Map map = cartFeign.list();
        return new Result(true, StatusCode.OK,"添加购物车成功",map);
    }
}
