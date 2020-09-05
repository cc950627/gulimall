package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 19:07:49
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseEntity> unreceiveList(Map<String, Object> params);

    boolean mergePuschase(PurchaseEntity purchase);

    boolean receive(List<Long> purchaseIds);

    boolean removePurchaseByIds(List<Long> purchaseIds);

    boolean finish(PurchaseDoneVO purchaseDoneVO);
}

