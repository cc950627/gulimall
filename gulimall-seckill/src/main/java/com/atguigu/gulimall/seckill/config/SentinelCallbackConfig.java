package com.atguigu.gulimall.seckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import com.atguigu.common.exception.BizExceptionEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;


@Configuration
public class SentinelCallbackConfig {

    @PostConstruct
    public void initSentinelCallback() {
        WebFluxCallbackManager.setBlockHandler((exchange, ex) ->
                ServerResponse.status(BizExceptionEnum.S_REQ_COVER_LIMIT_FLOW.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(BizExceptionEnum.S_REQ_COVER_LIMIT_FLOW.getMessage()), String.class));
    }
}
