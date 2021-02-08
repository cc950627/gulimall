package com.atguigu.gulimall.product.config;

import feign.RequestInterceptor;
import io.seata.core.context.RootContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            Optional.ofNullable(requestAttributes).ifPresent(e -> {
                requestTemplate.header("Cookie", e.getRequest().getHeader("Cookie"));
                // seata分布式事务相关
                if (StringUtils.isNotBlank(RootContext.getXID())) {
                    requestTemplate.header(RootContext.KEY_XID , RootContext.getXID());
                }
            });
        };
    }
}
