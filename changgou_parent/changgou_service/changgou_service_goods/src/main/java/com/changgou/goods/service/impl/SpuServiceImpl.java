package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.management.counter.perf.PerfInstrumentation;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param goods
     */
    @Transactional
    @Override
    public void add(Goods goods){
        Spu spu = goods.getSpu();
        //用工具类获取分布式商品id
        long id = idWorker.nextId();
        spu.setId(String.valueOf(id));
        spu.setIsDelete("0");
        spu.setIsMarketable("0");
        spu.setStatus("0");
        spuMapper.insertSelective(spu);
        //这是是增加了spu的数据,spu跟sku是一对多的关系,还要保存sku的数据到数据库
        this.saveSkuList(goods);
    }

    //保存skuList的数据到数据库,根据spu的id查询对应的sku集合,遍历保存
    private void saveSkuList(Goods goods) {
        Spu spu = goods.getSpu();
        //获取品牌对象
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        //获取分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //添加分类与品牌之间的关联
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if(count==0){
            //说明还没有关联关系,就将这两个id添加到数据库
            categoryBrandMapper.insertSelective(categoryBrand);
        }

        //获取sku
        List<Sku> skuList = goods.getSkuList();
        //用户不一定传入sku信息,所以sku有可能为null,所以要进行严谨性判断
        if(skuList !=null && skuList.size() > 0){
            for (Sku sku : skuList) {
                sku.setId(String.valueOf(idWorker.nextId()));

                //后面要将json格式的数据转换为bean对象,如果写null的话就会报错
                if(sku.getSpec()==null || "".equals(sku.getSpec())){
                    sku.setSpec("{}");
                }

                //设置sku的名称(名称有两部分组成:商品名称+规格)
                String name = spu.getName();
                Map<String,String> specMap = JSON.parseObject(sku.getSpec(),Map.class);
                if(specMap!=null){
                    for (String value : specMap.values()) {
                        name += "" + value;
                    }
                }

                sku.setName(name);      //商品名称
                sku.setSpuId(spu.getId());  //spuId
                sku.setCreateTime(new Date());  //创建日期
                sku.setUpdateTime(new Date());  //更新日期
                sku.setCategoryId(category.getId());    //分类id
                sku.setCategoryName(category.getName());    //分类名称
                sku.setBrandName(brand.getName());  //品牌名称


                //插入sku表数据
                skuMapper.insertSelective(sku);
            }
        }
    }


    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!"0".equals(spu.getIsMarketable())){
            //将商品下架
            spu.setIsMarketable("0");
        }
        //必须先下架商品再删除
        spu.setIsDelete("1");
        //未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

    //根据id查询商品goods
    @Override
    public Goods findGoodsById(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        /**
         * 为什么sku是根据条件查询?
         * 因为spu和sku是一对多的关系,根据传进来的spu的id查询多个sku,而example封装的查询条件就是spu的id
         */
        List<Sku> skuList = skuMapper.selectByExample(example);
        //封装返回
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    //修改保存商品
    @Transactional
    @Override
    public void update(Goods goods) {
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",spu.getId());
        skuMapper.deleteByExample(example);
        this.saveSkuList(goods);
    }

    //商品的审核
    @Transactional
    @Override
    public void audit(String id) {
        //查询spu是否存在
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            //商品不存在
            throw new RuntimeException("当前商品不存在");
        }
        if("1".equals(spu.getIsDelete())){
            throw  new RuntimeException("当前商品处于删除状态");
        }
        //将商品的审核状态改为已审核,上架状态改为已上架
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    //商品的下架
    @Transactional
    @Override
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            //商品不存在
            throw new RuntimeException("商品不存在");
        }
        if("1".equals(spu.getIsDelete())){
            throw new RuntimeException("当前商品处于删除状态");
        }
        //将商品的上下架状态改为下架
        spu.setIsMarketable("0");
        spu.setStatus("0");
        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    //商品上架
    @Transactional
    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!"1".equals(spu.getStatus())){
            throw new RuntimeException("审核未通过的商品不能上架");
        }
        //改变商品的上下架状态为1
        spu.setIsMarketable("1");
        //执行操作
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    //还原被删除的商品,逻辑删除
    @Override
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("商品未删除");
        }
        spu.setIsDelete("0");   //未删除
        spu.setStatus("0");     //未审核
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    //删除商品,物理删除
    @Override
    public void realDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("该商品未删除");
        }
        spuMapper.deleteByPrimaryKey(id);
    }

}
