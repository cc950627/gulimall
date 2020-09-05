package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.SkuReductionTO;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 商品满减信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:13:17
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean saveSkuReduction(List<SkuReductionTO> skuFullReductions);
}

