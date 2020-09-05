package com.atguigu.gulimall.member.config;

import com.atguigu.common.utils.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    /**
     * cookie跨域
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName(Constant.REDIS_DOMAIN_NAME);
        cookieSerializer.setCookieName(Constant.REDIS_COOKIE_NAME);
        return cookieSerializer;
    }

}
