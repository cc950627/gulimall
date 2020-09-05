package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SpuBoundTO;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTO bounds);

    @RequestMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody List<SkuInfoEntity> skus);

}
