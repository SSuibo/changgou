package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PackageName: com.changgou.order.service.impl
 * @ClassName: CartServiceImpl
 * @Author: suibo
 * @Date: 2020/1/13 20:47
 * @Description: //TODO
 */
@Service
public class CartServiceImpl implements CartService {

    //定义这个常量是为了区分大key和小key
    private static final String CART = "cart_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Override
    public void add(String skuId, Integer num, String username) {
        //首先从redis查询这个sku信息,如果存在就加数量,加价钱,不存在就添加
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(skuId);
        if (orderItem != null){
            //2.如果当前商品在redis中的存在,则更新商品的数量与价钱
            orderItem.setNum(orderItem.getNum()+num);
            if (orderItem.getNum()<=0){
                //删除该商品
                redisTemplate.boundHashOps(CART+username).delete(skuId);
                return;
            }
            orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum()*orderItem.getPrice());
        }else {
            //购物车中没有此商品,查询此商品信息
            Sku sku = skuFeign.findById(skuId).getData();
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //将sku和spu的信息封装到orderItem中
            orderItem = this.sku2OrderItem(sku,spu,num);
        }
        redisTemplate.boundHashOps(CART + username).put(skuId,orderItem);
    }

    //查询出来的sku及spu信息封装到orderItem中
    private OrderItem sku2OrderItem(Sku sku, Spu spu, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(orderItem.getPrice()*num);
        orderItem.setPayMoney(orderItem.getPrice()*num);
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()*num);
        //分类信息
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }

    //根据用户名查询购物车列表
    @Override
    public Map list(String username) {

        Map map = new HashMap();
        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART + username).values();
        map.put("orderItemList",orderItemList);

        Integer totalNum = 0;
        Integer totalMoney = 0;
        for (OrderItem orderItem : orderItemList) {
            //获取到购物项的数量及价格
            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
        }
        map.put("totalNum",totalNum);
        map.put("totalMoney",totalMoney);
        return map;
    }
}
