package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/getSkuStock")
    R getSkuStock(@RequestBody Collection<Long> skuIds);
}
