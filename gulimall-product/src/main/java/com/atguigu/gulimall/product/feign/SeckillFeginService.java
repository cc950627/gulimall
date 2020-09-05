package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFeginServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill", fallback = SeckillFeginServiceFallback.class)
public interface SeckillFeginService {

    @GetMapping("/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable Long skuId);
}
