package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.ware.constant.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.feign.SearchFeignService;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.PurchaseDetailVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> iPage = new Query<PurchaseEntity>().getPage(params);
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        IPage<PurchaseEntity> page = this.page(iPage, wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseEntity> unreceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).or().eq("status", 1);
        return this.list(wrapper);
    }

    @Transactional
    @Override
    public boolean mergePuschase(PurchaseEntity purchase) {
        List<PurchaseDetailEntity> purchaseDetails = purchaseDetailService.listByIds(purchase.getItems());
        Map<Boolean, List<PurchaseDetailEntity>> map = purchaseDetails.stream().collect(Collectors.partitioningBy(e ->
                Objects.equals(e.getStatus(), PurchaseStatusEnum.CREATE.getStatus()) || Objects.equals(e.getStatus(), PurchaseStatusEnum.ASSIGNED.getStatus())));
        if (CollectionUtils.isNotEmpty(map.get(false))) {
            Set<Long> purchaseIds = map.get(false).stream().map(PurchaseDetailEntity::getId).collect(Collectors.toSet());
            throw new BizException(BizExceptionEnum.W_PURCHASE_MAGER_FAIL, String.format("采购需求：%s已领取，不允许再分配", purchaseIds.toString()));
        }
        if (Objects.isNull(purchase.getPurchaseId())) {
            purchase.setStatus(PurchaseStatusEnum.CREATE.getStatus());
            purchase.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            purchase.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            this.save(purchase);
            purchase.setPurchaseId(purchase.getId());
        } else {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchase.getPurchaseId());
            purchaseEntity.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            this.updateById(purchaseEntity);
        }

        map.get(true).stream().forEach(e -> {
            e.setPurchaseId(purchase.getPurchaseId());
            e.setStatus(PurchaseStatusEnum.ASSIGNED.getStatus());
        });
        return purchaseDetailService.updateBatchById(map.get(true));
    }

    @Transactional
    @Override
    public boolean receive(List<Long> purchaseIds) {
        List<PurchaseEntity> purchases = this.listByIds(purchaseIds);
        List<PurchaseEntity> purchaseList = purchases.stream().filter(e -> Objects.equals(e.getStatus(), PurchaseStatusEnum.ASSIGNED.getStatus())).map(e -> {
            e.setStatus(PurchaseStatusEnum.RECEIVE.getStatus());
            e.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            return e;
        }).collect(Collectors.toList());

        List<Long> purchaseIdList = purchaseList.stream().map(PurchaseEntity::getId).collect(Collectors.toList());
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.in("purchase_id", purchaseIdList);
        List<PurchaseDetailEntity> purchaseDetails = purchaseDetailService.list(wrapper);
        Set<Long> purchaseIdSet = purchaseDetails.stream().map(PurchaseDetailEntity::getPurchaseId).collect(Collectors.toSet());
        Set<Long> purchaseEmptyIds = purchaseIdList.stream().filter(e -> !purchaseIdSet.contains(e)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(purchaseEmptyIds)) {
            throw new BizException(BizExceptionEnum.W_PURCHASE_RECEIVE_FAIL, String.format("采购订单：%s没有分配采购需求，不能领取", purchaseEmptyIds.toString()));
        }

        this.updateBatchById(purchaseList);
        purchaseDetails.stream().forEach(e -> e.setStatus(PurchaseStatusEnum.RECEIVE.getStatus()));
        return purchaseDetailService.updateBatchById(purchaseDetails);
    }

    @Transactional
    @Override
    public boolean removePurchaseByIds(List<Long> purchaseIds) {
        List<PurchaseEntity> purchases = this.listByIds(purchaseIds);
        Set<Long> purchaseIdSet = purchases.stream().filter(e -> !Objects.equals(e.getStatus(), PurchaseStatusEnum.CREATE.getStatus())
                && !Objects.equals(e.getStatus(), PurchaseStatusEnum.ASSIGNED.getStatus())).map(PurchaseEntity::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(purchaseIdSet)) {
            throw new BizException(BizExceptionEnum.W_PURCHASE_REMOVE_FAIL, String.format("采购订单：%s已被领取，不允许删除", purchaseIdSet.toString()));
        }
        this.removeByIds(purchaseIds);

        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.in("purchase_id", purchaseIds);
        List<PurchaseDetailEntity> PurchaseDetails = purchaseDetailService.list(wrapper);
        PurchaseDetails.forEach(e -> {
            e.setPurchaseId(0L);
            e.setStatus(PurchaseStatusEnum.CREATE.getStatus());
        });
        return purchaseDetailService.updateBatchById(PurchaseDetails);
    }

    @Transactional
    @Override
    public boolean finish(PurchaseDoneVO purchaseDoneVO) {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVO.getId());
        purchaseEntity.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        purchaseEntity.setStatus(PurchaseStatusEnum.FINISH.getStatus());
        List<PurchaseDetailVO> purchaseDetailVOs = purchaseDoneVO.getItems();
        Map<Integer, List<PurchaseDetailVO>> map = purchaseDetailVOs.stream().collect(Collectors.groupingBy(PurchaseDetailVO::getStatus));
        if (CollectionUtils.isNotEmpty(map.get(PurchaseStatusEnum.HASERROR.getStatus()))) {
            purchaseEntity.setStatus(PurchaseStatusEnum.HASERROR.getStatus());
        }
        this.updateById(purchaseEntity);

        List<PurchaseDetailEntity> purchaseDetails = purchaseDetailVOs.stream().map(e -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(e.getItemId());
            purchaseDetail.setStatus(e.getStatus());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetails);

        return wareSkuService.addStock(map.get(PurchaseStatusEnum.FINISH.getStatus()));
    }

}
