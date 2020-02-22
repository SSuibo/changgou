package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.CustomMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SecKillOrderService;
import com.changgou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @PackageName: com.changgou.seckill.service.impl
 * @ClassName: SecKillOrderServiceImpl
 * @Author: suibo
 * @Date: 2020/2/11 21:46
 * @Description: //TODO
 */
@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CustomMessageSender customMessageSender;


    private static final String SECKILL_KEY = "SecKillGoods_";
    private static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    /**
     * 秒杀下订单的方法
     * @param id
     * @param time
     * @param username
     * @return
     */
    @Override
    public boolean add(Long id, String time, String username) {
        //获取商品数据
        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_KEY + time).get(id);

        String redisStock = (String) redisTemplate.boundValueOps(SECKILL_GOODS_STOCK_COUNT_KEY + goods.getId()).get();
        if(StringUtils.isEmpty(redisStock)){
            return false;
        }
        int value = Integer.parseInt(redisStock);

        //如果没有库存,则直接抛出异常
        if(goods==null || value<=0){
            return false;
        }

        //redis预扣库存
        Long stockCount = redisTemplate.boundValueOps(SECKILL_GOODS_STOCK_COUNT_KEY + goods.getId()).decrement();

        if(stockCount<=0){
            //库存没有了,删除商品信息
            redisTemplate.boundHashOps(SECKILL_KEY+time).delete(id);

            //删除对应的库存信息
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + goods.getId());
        }

        //有库存,创建秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(goods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(goods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //发送消息
        customMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_KEY, JSON.toJSONString(seckillOrder));
        return true;
    }
}
