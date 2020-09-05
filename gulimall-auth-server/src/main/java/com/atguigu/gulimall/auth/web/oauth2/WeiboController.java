package com.atguigu.gulimall.auth.web.oauth2;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdParytFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Objects;

@RequestMapping("/oauth2/weibo")
@Controller
public class WeiboController {

    @Autowired
    private ThirdParytFeignService thirdParytFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @RequestMapping("/getAccessToken")
    public String getAccessToken(@RequestParam("code") String code, HttpSession session) {
        R r = thirdParytFeignService.getWeiboAccessToken(code);
        if (!Objects.equals(0, r.getCode())) {
            return "redirect:http://auth.gulimall.com/login.html";
        }
        String accessToken = String.valueOf(r.get("data"));

        R r2 = memberFeignService.weiboLogin(accessToken);
        if (!Objects.equals(0, r2.getCode())) {
            return "redirect:http://auth.gulimall.com/login.html";
        }
        session.setAttribute(Constant.REDIS_COOKIE_NAME, r2.get("data"));
        return "redirect:http://gulimall.com";
    }

}
