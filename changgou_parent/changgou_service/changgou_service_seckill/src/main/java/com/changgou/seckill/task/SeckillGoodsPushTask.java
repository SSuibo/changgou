package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @PackageName: com.changgou.seckill.task
 * @ClassName: SeckillGoodsPushTask
 * @Author: suibo
 * @Date: 2020/2/10 19:18
 * @Description: //定时任务定时将审核通过的订单添加到redis,因为秒杀是高并发的业务从redis可以减少数据库的压力
 */
@Component
public class SeckillGoodsPushTask {

    private static final String SECKILL_KEY = "SecKillGoods_";
    private static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/30 * * * * ?")    //30秒扫描一次
    public void loadSecKillGoodsToRedis(){
        //获取当页显示的5个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String redisExtName = DateUtil.date2Str(dateMenu);

            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status","1");  //审核通过
            criteria.andGreaterThan("stockCount",0);//库存大于0
            criteria.andGreaterThanOrEqualTo("startTime",simpleDateFormat.format(dateMenu));    //开始时间大于等于当前时间
            criteria.andLessThan("endTime",simpleDateFormat.format(DateUtil.addDateHour(dateMenu,2)));  //结束时间小于当前时间+2小时

            Set keys = redisTemplate.boundHashOps(SECKILL_KEY + redisExtName).keys();
            //移除掉缓存中已经有的重复的商品
            if(keys!=null && keys.size()>0){
                criteria.andNotIn("id",keys);
            }

            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            //添加到缓存中
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps(SECKILL_KEY+redisExtName).put(seckillGood.getId(),seckillGood);

                //预加载库存信息
                redisTemplate.opsForValue().set(SECKILL_GOODS_STOCK_COUNT_KEY + seckillGood.getId(),seckillGood.getStockCount());
            }
        }
    }
}
