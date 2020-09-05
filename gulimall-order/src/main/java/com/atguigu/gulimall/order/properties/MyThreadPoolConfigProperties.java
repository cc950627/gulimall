package com.atguigu.gulimall.order.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.thread.pool")
@Component
@Data
public class MyThreadPoolConfigProperties {

    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Long keepAliveTime;
    private Integer capacity;
}
