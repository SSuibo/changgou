package com.changgou.goods.pojo;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * @PackageName: com.changgou.goods.pojo
 * @ClassName: Goods
 * @Author: suibo
 * @Date: 2020/1/2 19:23
 * @Description: //页面传过来的spu表信息,和sku表信息封装成一个实体类,spu和sku是一个一对多的关系
 */
public class Goods implements Serializable {

    private Spu spu;
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
