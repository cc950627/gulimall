package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.to.UserInfoTO;
import com.atguigu.common.to.mq.SeckillOrderTO;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.constant.SeckillConstant;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTO;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSkusVO;
import com.atguigu.gulimall.seckill.vo.SeckillSkuRelationVO;
import com.atguigu.gulimall.seckill.vo.SkuInfoVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkulatest3Days() {
        R r = couponFeignService.seckillSessionService();
        List<SeckillSessionWithSkusVO> data = JSON.parseArray(JSON.toJSONString(r.get("data")), SeckillSessionWithSkusVO.class);
        saveSessionInfo(data);
        saveSessionSkuInfo(data);
    }

    @Override
    public List<SeckillSkuRedisTO> getCurrentSeckillSkus() {
        long now = System.currentTimeMillis();
        RMap<String, String> rMap = redissonClient.getMap(Constant.REDIS_SKUKILL_CACHE_PREFIX);
        List<SeckillSkuRedisTO> seckillSkuRedisTOS = redissonClient.getKeys().getKeysStreamByPattern(Constant.REDIS_SESSION_CACHE_PREFIX + "*").map(e -> {
            String replace = e.replace(Constant.REDIS_SESSION_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long startTime = Long.parseLong(split[0]);
            long endTime = Long.parseLong(split[1]);
            if (now > startTime && now < endTime) {
                RList<String> rList = redissonClient.getList(Constant.REDIS_SESSION_CACHE_PREFIX + String.format("%s_%s", startTime, endTime));
                return rList.stream().map(o -> JSON.parseObject(rMap.getAsync(o).join(), SeckillSkuRedisTO.class)).collect(Collectors.toList());
            }
            return new ArrayList<SeckillSkuRedisTO>();
        }).flatMap(Collection::stream).collect(Collectors.toList());
        return seckillSkuRedisTOS;
    }

    @Override
    public SeckillSkuRedisTO getSkuSeckillInfo(Long skuId) {
        RMap<String, String> rMap = redissonClient.getMap(Constant.REDIS_SKUKILL_CACHE_PREFIX);
        String regx = "\\d_" + skuId;
        AtomicReference<SeckillSkuRedisTO> atomic = new AtomicReference<>();
        rMap.keySet().stream().filter(e -> Pattern.matches(regx, e)).findAny().ifPresent(e -> {
            SeckillSkuRedisTO seckillSkuRedisTO = JSON.parseObject(rMap.get(e), SeckillSkuRedisTO.class);
            Long startTime = seckillSkuRedisTO.getStartTime();
            Long endTime = seckillSkuRedisTO.getEndTime();
            long now = System.currentTimeMillis();
            if (now < startTime || now > endTime) {
                seckillSkuRedisTO.setRandomCode(null);
            }
            atomic.set(seckillSkuRedisTO);
        });
        return atomic.get();
    }

    @Override
    public String seckill(String killId, String key, Integer num) {
        UserInfoTO loginUser = LoginUserInterceptor.loginUser.get();

        RMap<String, String> rMap = redissonClient.getMap(Constant.REDIS_SKUKILL_CACHE_PREFIX);
        String json = rMap.get(killId);
        Optional.ofNullable(json).orElseThrow(() -> new BizException(BizExceptionEnum.S_PRODUCT_NOT_EXITS));

        SeckillSkuRedisTO seckillSkuRedisTO = JSON.parseObject(rMap.get(killId), SeckillSkuRedisTO.class);
        Long startTime = seckillSkuRedisTO.getStartTime();
        Long endTime = seckillSkuRedisTO.getEndTime();
        long now = System.currentTimeMillis();
        if (now < startTime || now > endTime) {
            throw new BizException(BizExceptionEnum.S_NOT_ACTIVITY_DATE);
        }
        String randomCode = seckillSkuRedisTO.getRandomCode();
        if (!Objects.equals(randomCode, key)) {
            throw new BizException(BizExceptionEnum.S_RANDOMCODE_CHECK_FAIL);
        }
        String skuId = String.format("%s_%s", seckillSkuRedisTO.getPromotionSessionId(), seckillSkuRedisTO.getSkuId());
        if (!Objects.equals(skuId, killId)) {
            throw new BizException(BizExceptionEnum.S_PRODUCTINFO_CHECK_FAIL);
        }
        Integer seckillLimit = seckillSkuRedisTO.getSeckillLimit();
        if (num > seckillLimit) {
            throw new BizException(BizExceptionEnum.S_PRODUCT_NUMBER_OUT_LIMIT);
        }
        String userKey = String.format("%s_%s", loginUser.getId(), skuId);
        Boolean isSeckill = redissonClient.getBucket(userKey).trySetAsync(num, endTime - startTime, TimeUnit.MILLISECONDS).join();
        if (!isSeckill) {
            throw new BizException(BizExceptionEnum.S_PRODCUT_ALREADY_BUY);
        }
        RFuture<Boolean> rFuture = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + key).tryAcquireAsync(num);
        if (!rFuture.join()) {
            throw new BizException(BizExceptionEnum.S_PRODCUT_SOLD_EMPTY);
        }
        String orderSn = IdWorker.getTimeId();
        SeckillOrderTO seckillOrderTO = new SeckillOrderTO();
        seckillOrderTO.setOrderSn(orderSn);
        seckillOrderTO.setSkuId(seckillSkuRedisTO.getSkuId());
        seckillOrderTO.setPromotionSessionId(seckillSkuRedisTO.getPromotionSessionId());
        seckillOrderTO.setNum(num);
        seckillOrderTO.setMemberId(loginUser.getId());
        seckillOrderTO.setSeckillPrice(seckillSkuRedisTO.getSeckillPrice());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTO);

        return orderSn;
    }

    private void saveSessionInfo(List<SeckillSessionWithSkusVO> data) {
        data.forEach(e -> {
            long startTime = e.getStartTime().getTime();
            long endTime = e.getEndTime().getTime();
            String key = Constant.REDIS_SESSION_CACHE_PREFIX + String.format("%s_%s", startTime, endTime);
            if (!redissonClient.getList(key).isExistsAsync().join()) {
                Set<String> value = e.getSeckillSkuRelations().stream().map(o ->
                        String.format("%s_%s", o.getPromotionSessionId(), o.getSkuId())).collect(Collectors.toSet());
                redissonClient.getList(key).addAllAsync(value);
            }
        });
    }

    private void saveSessionSkuInfo(List<SeckillSessionWithSkusVO> data) {
        List<Long> skuIds = data.stream().map(e -> e.getSeckillSkuRelations().stream().map(SeckillSkuRelationVO::getSkuId)
                .collect(Collectors.toSet())).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        R r = productFeignService.listBySkuIds(skuIds);
        List<SkuInfoVO> skuInfoVOS = JSON.parseArray(JSON.toJSONString(r.get("data")), SkuInfoVO.class);

        RMap<String, String> rMap = redissonClient.getMap(Constant.REDIS_SKUKILL_CACHE_PREFIX);
        data.forEach(e -> e.getSeckillSkuRelations().forEach(o -> {
            SeckillSkuRedisTO seckillSkuRedisTO = new SeckillSkuRedisTO();
            skuInfoVOS.stream().filter(x -> Objects.equals(x.getSkuId(), o.getSkuId())).findAny().ifPresent(seckillSkuRedisTO::setSkuInfoVO);
            seckillSkuRedisTO.setStartTime(e.getStartTime().getTime());
            seckillSkuRedisTO.setEndTime(e.getEndTime().getTime());
            String uuid = UuidUtils.generateUuid();
            seckillSkuRedisTO.setRandomCode(uuid);
            BeanUtils.copyProperties(o, seckillSkuRedisTO);
            String join = rMap.putIfAbsentAsync(String.format("%S_%s", o.getPromotionSessionId(), o.getSkuId()), JSON.toJSONString(seckillSkuRedisTO)).join();
            if (Objects.isNull(join)) {
                redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + uuid).trySetPermitsAsync(o.getSeckillCount());
            }
        }));
    }
}
