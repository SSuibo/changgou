package com.changgou.seckill.service.impl;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @PackageName: com.changgou.seckill.service.impl
 * @ClassName: SecKillGoodsServiceImpl
 * @Author: suibo
 * @Date: 2020/2/10 21:02
 * @Description: //TODO
 */
@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SECKILL_KEY = "SecKillGoods_";

    @Override
    public List<SeckillGoods> list(String time) {
        return redisTemplate.boundHashOps(SECKILL_KEY + time).values();
    }
}
