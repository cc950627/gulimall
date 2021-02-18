package com.atguigu.gulimall.product;

import com.alibaba.cloud.seata.feign.SeataFeignClientAutoConfiguration;
import io.seata.config.springcloud.EnableSeataSpringConfig;
import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableSeataSpringConfig
@EnableRedissonHttpSession
@EnableTransactionManagement
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.atguigu.gulimall.product", exclude = {SeataFeignClientAutoConfiguration.class})
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
