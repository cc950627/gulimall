package com.atguigu.gulimall.thirdparyt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Component
@Data
public class SmsComponentProperties {

    private String host;

    private String path;

    private String sign;

    private String skin;

    private String appcode;
}
