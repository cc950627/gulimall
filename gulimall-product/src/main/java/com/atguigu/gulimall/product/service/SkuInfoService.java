package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.query.ProductQuery;
import com.atguigu.gulimall.product.vo.SkuItemVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:45
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCoundtion(Map<String, Object> params);

    List<SkuInfoEntity> listByCoundtion(ProductQuery params);

    List<Long> getCatelogIdsBySkuId(Long skuId);

    SkuItemVO skuItem(Long skuId);

    BigDecimal getPrice(Long skuId);
}

