package com.changgou.consumer.service.impl;

import com.changgou.consumer.dao.SeckillGoodsMapper;
import com.changgou.consumer.dao.SeckillOrderMapper;
import com.changgou.consumer.service.SeckillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @PackageName: com.changgou.consumer.service.impl
 * @ClassName: SeckillOrderServiceImpl
 * @Author: suibo
 * @Date: 2020/2/12 19:57
 * @Description: //TODO
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Transactional
    @Override
    public int createOrder(SeckillOrder seckillOrder) {
        int result = seckillGoodsMapper.updateStockCount(seckillOrder.getSeckillId());
        if(result<=0){
            return result;
        }
        result = seckillOrderMapper.insertSelective(seckillOrder);

        if(result<=0){
            return result;
        }
        return 1;
    }
}
