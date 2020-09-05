package com.atguigu.gulimall.thirdparyt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.oauth2.weibo")
@Component
@Data
public class OAuth2ComponentProperties {

    private String clientId;

    private String clientSecret;

    private String grantType;

    private String redirectUri;

    private String weiboUri;
}
