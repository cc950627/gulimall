package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeginService;
import org.springframework.stereotype.Component;

@Component
public class SeckillFeginServiceFallback implements SeckillFeginService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        return R.error(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL.getCode(), BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL.getMessage());
    }
}
