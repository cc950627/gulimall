package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.to.WareSkuLockTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/getSkuStock")
    R getSkuStock(@RequestBody Collection<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/orderLockStock")
    R orderLockStock(@RequestBody WareSkuLockTO wareSkuLockTO);
}
