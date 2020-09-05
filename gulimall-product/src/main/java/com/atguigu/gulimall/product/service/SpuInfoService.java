package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * spu信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean saveSpuInfo(SpuInfoEntity spuInfo);

    PageUtils queryPageByCoundtion(Map<String, Object> params);

    boolean prdouctUp(Long spuId);

    SpuInfoEntity getSpuInfo(Long skuId);
}

