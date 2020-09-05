package com.atguigu.gulimall.thirdparyt.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparyt.component.OAuth2Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/oauth2/weibo")
@RestController
public class WeiboController {

    @Autowired
    private OAuth2Component oauth2Component;

    @GetMapping("/getAccessToken")
    public R getWeiboAccessToken(String code) {
        String weiboAccessToken = oauth2Component.getWeiboAccessToken(code);
        System.out.println(weiboAccessToken);
        return R.ok().put("data", weiboAccessToken);
    }

    @GetMapping("/getUserInfo")
    public R getWeiboUserInfo(@RequestParam("accessToken") String accessToken, @RequestParam("uid") String uid) {
        String userInfo = oauth2Component.getWeiboUserInfo(accessToken, uid);
        System.out.println(userInfo);
        return R.ok().put("data", userInfo);
    }

}
