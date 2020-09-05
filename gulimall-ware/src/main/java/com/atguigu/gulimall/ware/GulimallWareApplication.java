package com.atguigu.gulimall.ware;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.seata.config.springcloud.EnableSeataSpringConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableSeataSpringConfig
@EnableFeignClients(basePackages = "com.atguigu.gulimall.ware.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.ware.dao")
@SpringBootApplication
public class GulimallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWareApplication.class, args);
    }

}
