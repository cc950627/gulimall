package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-paryt")
public interface ThirdParytFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);

    @GetMapping("/oauth2/weibo/getAccessToken")
    R getWeiboAccessToken(@RequestParam("code") String code);
}
