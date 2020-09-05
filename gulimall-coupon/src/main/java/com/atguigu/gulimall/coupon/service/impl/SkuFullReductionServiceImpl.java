package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.SkuReductionTO;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private SkuFullReductionService skuFullReductionService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean saveSkuReduction(List<SkuReductionTO> skuFullReductions) {
        List<SkuLadderEntity> skuLadders = Lists.newArrayList();
        List<SkuFullReductionEntity> skuFullReductionList = Lists.newArrayList();
        List<MemberPriceEntity> memberPrices = Lists.newArrayList();

        Optional<List<SkuReductionTO>> optional = Optional.ofNullable(skuFullReductions);
        optional.ifPresent(e -> e.stream().forEach(o -> {
            if (o.getFullCount() > 0) {
                SkuLadderEntity skuLadder = new SkuLadderEntity();
                BeanUtils.copyProperties(o, skuLadder);
                skuLadder.setAddOther(o.getCountStatus());
                skuLadders.add(skuLadder);
            }


            if (Objects.equals(o.getFullPrice().compareTo(BigDecimal.valueOf(0)), 1)) {
                SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
                BeanUtils.copyProperties(o, skuFullReduction);
                skuFullReductionList.add(skuFullReduction);
            }

            Optional.ofNullable(o.getMemberPrice()).ifPresent(x -> x.stream().filter(y -> Objects.equals(y.getPrice()
                    .compareTo(BigDecimal.valueOf(0)), 1)).forEach(y -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(o.getSkuId());
                memberPriceEntity.setMemberLevelId(y.getId());
                memberPriceEntity.setMemberLevelName(y.getName());
                memberPriceEntity.setMemberPrice(y.getPrice());
                memberPriceEntity.setAddOther(1);
                memberPrices.add(memberPriceEntity);
            }));
        }));

        skuLadderService.saveBatch(skuLadders);
        skuFullReductionService.saveBatch(skuFullReductionList);
        memberPriceService.saveBatch(memberPrices);
        return true;
    }

}
