package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.config.RabbitMqConfig;
import com.changgou.order.dao.*;
import com.changgou.order.pojo.*;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.pay.feign.WxPayFeign;
import com.changgou.user.feign.UserFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param order
     */
    @GlobalTransactional(name = "order_add")    //分布式事务注解
    @Override
    public String add(Order order){
        //获取购物车数据(购物项列表,总金额,总数量)
        Map map = cartService.list(order.getUsername());
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        //填充订单数据
        order.setTotalMoney((Integer) map.get("totalMoney"));
        order.setTotalNum((Integer) map.get("totalNum"));
        //用雪花算法来生成这个分布式的Id
        String id = idWorker.nextId() + "";
        order.setId(id);
        order.setUpdateTime(order.getCreateTime());
        order.setBuyerRate("0");    //0:未评价  1:已评价
        order.setSourceType("1");   //1:web
        order.setOrderStatus("0");  //0:未完成  1:已完成  2:已退货
        order.setPayStatus("0");    //0:未支付  1:已支付
        order.setConsignStatus("0");//0:未发货  1:已发货
        order.setCreateTime(new Date());    //订单创建时间
        //补充完信息之后添加到order表中
        orderMapper.insertSelective(order);

        //补充购物项数据
        for (OrderItem orderItem : orderItemList) {
            orderItem.setId(id);
            orderItem.setIsReturn("0"); //未退货
            orderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(orderItem);
        }

        skuFeign.decrCount(order.getUsername());

        //userFeign.addPoints(10);
        //下订单时往添加积分的任务表中添加数据
        System.out.println("往订单数据库中的任务表添加数据");
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        //设置交换机
        task.setMqExchange(RabbitMqConfig.EX_BUYING_ADDPOINTUSER);
        //设置路由key
        task.setMqRoutingkey(RabbitMqConfig.CG_BUYING_ADDPOINT_key);

        Map taskMap = new HashMap();
        taskMap.put("username",order.getUsername());
        taskMap.put("orderId",id);
        taskMap.put("point",order.getTotalMoney());

        task.setRequestBody(JSON.toJSONString(taskMap));

        //调用持久层将数据添加到任务表中
        taskMapper.insertSelective(task);
        //最后删除购物车数据
        //redisTemplate.delete("cart_" + order.getUsername());
        rabbitTemplate.convertAndSend("","queue.ordercreate",id);
        return id;
    }


    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Order>)orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Order>)orderMapper.selectByExample(example);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andEqualTo("payType",searchMap.get("payType"));
           	}
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
           	}
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
           	}
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
           	}
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
           	}
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
           	}
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
           	}
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
           	}
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
           	}
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andEqualTo("sourceType",searchMap.get("sourceType"));
           	}
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
           	}
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andEqualTo("orderStatus",searchMap.get("orderStatus"));
           	}
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andEqualTo("payStatus",searchMap.get("payStatus"));
           	}
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andEqualTo("consignStatus",searchMap.get("consignStatus"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

    @Autowired
    private OrderLogMapper orderLogMapper;

    //修改订单状态
    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null && "0".equals(order.getPayStatus())){
            //查询出了订单,并且是未支付状态
            order.setPayStatus("1");
            order.setOrderStatus("1");
            order.setUpdateTime(new Date());
            order.setPayTime(new Date());
            order.setTransactionId(transactionId);  //交易流水号
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOrderId(order.getId());
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("支付流水号:" + transactionId);
            orderLogMapper.insert(orderLog);
        }
    }

    @Autowired
    private WxPayFeign wxPayFeign;

    //关闭订单
    @Override
    @Transactional
    public void closeOrder(String orderId) {
        System.out.println("关闭订单开启" + orderId);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null){
            throw new RuntimeException("订单不存在");
        }
        if("0".equals(order.getPayStatus())){
            //未支付
            throw new RuntimeException("当前订单不需要关闭");
        }
        System.out.println("关闭订单校验通过");

        //调用微信订单查询,查询订单支付状态
        Map wxQueryMap = (Map) wxPayFeign.queryOrder(orderId);
        System.out.println("查询微信支付订单" + wxQueryMap);

        if("SUCCESS".equals(wxQueryMap.get("trade_state"))){
            //支付状态是成功,进行补偿
            updatePayStatus(orderId, (String)wxQueryMap.get("transaction_id"));
            System.out.println("补偿");
        }

        if("NOTPAY".equals(wxQueryMap.get("trade_state"))){
            //未支付,关闭订单
            System.out.println("执行关闭");
            order.setCloseTime(new Date());//关闭时间
            order.setOrderStatus("4");  //关闭状态

            orderMapper.updateByPrimaryKeySelective(order);

            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setPayStatus("4");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);

            //恢复库存和销量
            OrderItem _orderItem = new OrderItem();
            _orderItem.setOrderId(orderId);

            List<OrderItem> orderItemList = orderItemMapper.select(_orderItem);

            for (OrderItem orderItem : orderItemList) {
                skuFeign.resumeStockNum(orderItem.getSkuId(),orderItem.getNum());
            }

            //关闭微信订单
            wxPayFeign.closeOrder(orderId);
        }
    }

    /**
     * 批量发货
     * @param orders
     */
    @Override
    @Transactional
    public void batchSend(List<Order> orders) {

        for (Order order : orders) {
            //判断运单号和物流公司是否为空
            if(order.getId()==null){
                throw new RuntimeException("订单号为空");
            }
            if(order.getShippingCode()==null || order.getShippingName()==null){
                throw new RuntimeException("请选择快递公司和填写快递单号");
            }
        }

        //循环订单,进行状态校验
        for (Order order : orders) {
            Order order1 = orderMapper.selectByPrimaryKey(order.getId());
            if(!"0".equals(order1.getConsignStatus()) || !"1".equals(order1.getOrderStatus())){
                throw new RuntimeException("订单状态有误");
            }
        }

        //循环订单,更新操作
        for (Order order : orders) {
            order.setOrderStatus("2");  //订单状态:已发货
            order.setConsignStatus("1");    //发货状态:已发货
            order.setConsignTime(new Date());//发货时间
            order.setUpdateTime(new Date());//更新时间
            orderMapper.updateByPrimaryKeySelective(order);

            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(idWorker.nextId() + "");
            orderLog.setOperateTime(new Date());//操作时间
            orderLog.setOperater("admin");  //系统管理员
            orderLog.setOrderStatus("2");   //已完成
            orderLog.setConsignStatus("1"); //发货状态("0"未发货   "1"已发货)
            orderLog.setOrderId(order.getId());
            orderLogMapper.insertSelective(orderLog);
        }
    }

    //手动确认收获
    @Override
    public void confirmTask(String orderId, String operator) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null){
            throw new RuntimeException("订单不存在");
        }
        if(!"1".equals(order.getConsignStatus())){
            throw new RuntimeException("订单未发货");
        }
        order.setConsignStatus("2");//已送达
        order.setOrderStatus("3");  //已完成
        order.setUpdateTime(new Date());
        order.setEndTime(new Date());   //交易结束
        orderMapper.updateByPrimaryKeySelective(order);

        //记录订单变动日志
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setOperateTime(new Date());
        orderLog.setOperater(operator); //当前订单时谁操作的,系统或者管理员或者用户
        orderLog.setOrderStatus("3");
        orderLog.setOrderId(order.getId());

        orderLogMapper.insertSelective(orderLog);
    }

    @Autowired
    private OrderConfigMapper orderConfigMapper;
    //自动确认收货
    @Transactional
    @Override
    public void autoTack() {

        //读取订单配置信息
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);

        //获得时间节点
        LocalDate now = LocalDate.now();//当前时间

        //获取过期的时间节点,在这个日期前发货的未收货订单
        LocalDate date = now.plusDays(-orderConfig.getTakeTimeout());
        System.out.println(date);

        //按条件查询过期订单
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("consignTime",date);
        criteria.andEqualTo("orderStatus","2");

        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
            System.out.println("过期订单:" + order.getId() + ","+order.getConsignStatus());
            this.confirmTask(order.getId(),"system");
        }
    }

}
