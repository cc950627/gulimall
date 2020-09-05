package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTO;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkulatest3Days();

    List<SeckillSkuRedisTO> getCurrentSeckillSkus();

    SeckillSkuRedisTO getSkuSeckillInfo(Long skuId);

    String seckill(String killId, String key, Integer num);
}
