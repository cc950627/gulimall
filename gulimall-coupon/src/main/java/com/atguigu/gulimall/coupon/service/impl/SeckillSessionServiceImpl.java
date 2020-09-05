package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> seckillSessionService() {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();
        String startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm ss"));
        String endTime = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm ss"));
        queryWrapper.between("start_time", startTime, endTime);
        List<SeckillSessionEntity> seckillSessions = this.list(queryWrapper);
        Set<Long> ids = seckillSessions.stream().map(SeckillSessionEntity::getId).collect(Collectors.toSet());

        QueryWrapper<SeckillSkuRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.in("promotion_session_id", ids);
        List<SeckillSkuRelationEntity> seckillSkuRelations = seckillSkuRelationService.list(wrapper);
        Map<Long, List<SeckillSkuRelationEntity>> map = seckillSkuRelations.stream().collect(Collectors.groupingBy(SeckillSkuRelationEntity::getPromotionSessionId));
        seckillSessions.stream().peek(e -> e.setSeckillSkuRelations(map.get(e.getId())));
        return seckillSessions;
    }

}
