package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.vo.AccessTokenVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-paryt")
public interface ThirdParytFeignService {

    @GetMapping("/oauth2/weibo/getUserInfo")
    R getWeiboUserInfo(@RequestParam("accessToken") String accessToken, @RequestParam("uid") String uid);

}
