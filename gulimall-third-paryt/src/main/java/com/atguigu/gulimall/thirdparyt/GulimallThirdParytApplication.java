package com.atguigu.gulimall.thirdparyt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GulimallThirdParytApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallThirdParytApplication.class, args);
    }

}
