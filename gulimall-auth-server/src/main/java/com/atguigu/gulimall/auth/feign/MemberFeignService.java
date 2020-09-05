package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.UserLoginVO;
import com.atguigu.gulimall.auth.vo.UserRegistVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/saveUserInfo")
    R saveUserInfo(@RequestBody UserRegistVO userRegistVO);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVO userLoginVO);

    @GetMapping("/member/member/weiboLogin")
    R weiboLogin(@RequestParam("accessToken") String accessToken);
}
