package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSkuRelationEntity> iPage = new Query<SeckillSkuRelationEntity>().getPage(params);
        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            queryWrapper.and(e -> e.eq("id", key).or().like("promotionId", key));
        }
        Object promotionSessionId = params.get("promotionSessionId");
        if (Objects.nonNull(promotionSessionId) && StringUtils.isNotBlank(String.valueOf(promotionSessionId))) {
            queryWrapper.and(e -> e.eq("", key).or().like("attr_group_name", key));
        }
        IPage<SeckillSkuRelationEntity> page = this.page(iPage, queryWrapper);

        return new PageUtils(page);
    }

}
