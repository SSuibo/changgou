package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @PackageName: com.changgou.controller
 * @ClassName: OrderController
 * @Author: suibo
 * @Date: 2020/1/15 1:24
 * @Description: //TODO
 */
@Controller
@RequestMapping("/worder")
public class OrderController {

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private AddressFeign addressFeign;

    @Autowired
    private OrderFeign orderFeign;

    @RequestMapping("/ready/order")
    public String readyOrder(){
        return "order";
    }

    @GetMapping("/findData")
    @ResponseBody
    public Result<Map> findData(){
        //结算页面需要购物项信息
        Map map = cartFeign.list();
        //也需要当前登陆人的地址信息
        List<Address> addressList = addressFeign.list().getData();
        map.put("addressList",addressList);
        return new Result<Map>(true, StatusCode.OK,"查询结算页清单成功",map);
    }

    //添加订单
    @PostMapping("/add")
    @ResponseBody
    public Result add(@RequestBody Order order){
        return orderFeign.add(order);
    }

    //跳转到选择支付页面
    @GetMapping("/toPayPage")
    public String toPayPage(String orderId,Model model){//用简单模式.String接受的前台数据,所有可以不写@PathVariable
        Result<Order> result = orderFeign.findById(orderId);
        Order order = result.getData();
        model.addAttribute("orderId",orderId);
        model.addAttribute("payMoney",order.getTotalMoney());
        return "pay";
    }
}
