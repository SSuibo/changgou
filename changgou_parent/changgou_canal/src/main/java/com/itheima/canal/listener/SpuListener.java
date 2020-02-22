package com.itheima.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.itheima.canal.config.RabbitMqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZJ
 */
@CanalEventListener     //声明这是canal的监听类
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param eventType     这个参数表示当前操作数据库的类型
     * @param rowData       这个参数表示当前操作的数据库的表
     */

    @ListenPoint(schema = "changgou_goods",table = "tb_spu")
    public void recieveMessage(CanalEntry.EventType eventType, CanalEntry.RowData rowData){

        //获取更新前的数据,封装到map集合里面
        Map<String,String> oldData = new HashMap<>();
        rowData.getBeforeColumnsList().forEach((c) -> oldData.put(c.getName(),c.getValue()));

        //获取更新之后的数据,封装到map集合
        Map<String,String> newMap = new HashMap<>();
        rowData.getAfterColumnsList().forEach(c -> newMap.put(c.getName(),c.getValue()));

        //商品上架,也就是spu表的isMarketable的状态从0->1
        if("0".equals(oldData.get("is_marketable")) && "1".equals(newMap.get("is_marketable"))){
            //表示商品上架, 发送spu表的id消息到rabbitMQ
            rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_UP_EXCHANGE,"",newMap.get("id"));
        }

        //商品下架,也就是spu表的isMarketable的状态从1->0
        if("1".equals(oldData.get("is_marketable")) && "0".equals(newMap.get("is_marketable"))){
            //表示商品下架, 发送spu表的id消息到rabbitMQ
            rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_DOWN_EXCHANGE,"",newMap.get("id"));
        }

        //商品上架,生成静态页面,也就是spu表的isMarketable的状态从0->1
        if("0".equals(oldData.get("status")) && "1".equals(newMap.get("status"))){
            //表示商品上架, 发送spu表的id消息到rabbitMQ
            rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_UP_EXCHANGE,"",newMap.get("id"));
        }
    }
}
