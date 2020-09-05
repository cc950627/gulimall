package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.ware.constant.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> iPage = new Query<PurchaseDetailEntity>().getPage(params);
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            wrapper.and(e -> e.eq("purchase_id", key).or().like("sku_id", key));
        }
        Object status = params.get("status");
        if (Objects.nonNull(status) && StringUtils.isNotBlank(String.valueOf(status))) {
            wrapper.eq("status", Integer.parseInt(String.valueOf(status)));
        }
        Object wareId = params.get("wareId");
        if (Objects.nonNull(wareId) && StringUtils.isNotBlank(String.valueOf(wareId))) {
            wrapper.eq("ware_id", Integer.parseInt(String.valueOf(wareId)));
        }
        wrapper.orderByAsc("status");
        IPage<PurchaseDetailEntity> page = this.page(iPage, wrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean removePurchasedetailByIds(List<Long> purchasedetailIds) {
        List<PurchaseDetailEntity> purchaseDetails = this.listByIds(purchasedetailIds);
        Set<Long> purchasedetailIdSet = purchaseDetails.stream().filter(e -> !Objects.equals(e.getStatus(), PurchaseStatusEnum.CREATE.getStatus())
                && !Objects.equals(e.getStatus(), PurchaseStatusEnum.ASSIGNED)).map(PurchaseDetailEntity::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(purchasedetailIdSet)) {
            throw new BizException(BizExceptionEnum.W_PURCHASEDETAIL_REMOVE_FAIL, String.format("采购需求：%s已被领取，不允许删除", purchasedetailIdSet.toString()));
        }
        return this.removeByIds(purchasedetailIds);
    }

}
