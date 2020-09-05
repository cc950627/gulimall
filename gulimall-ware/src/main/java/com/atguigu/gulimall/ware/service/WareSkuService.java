package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.PurchaseDetailVO;
import com.atguigu.gulimall.ware.vo.WareSkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 19:07:49
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean addStock(List<PurchaseDetailVO> purchaseDetailVOS);

    Map<Long, Integer> getSkuStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockVO wareSkuLockVO);
}

