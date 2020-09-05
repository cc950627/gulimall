package com.atguigu.gulimall.auth.web;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdParytFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVO;
import com.atguigu.gulimall.auth.vo.UserRegistVO;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private ThirdParytFeignService thirdParytFeignService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MemberFeignService memberFeignService;

    @RequestMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        RBucket<Object> bucket = redissonClient.getBucket(Constant.REDIS_SMS_CODE_TIMEOUT_PREFIX + phone);
        if (bucket.isExists())
            throw new BizException(BizExceptionEnum.AS_NOT_SEND_CODE, String.format("请%s秒后再发送验证码", (int)Math.ceil(bucket.remainTimeToLive() / 1000)));

        String code = UuidUtils.generateUuid().substring(0, 6);
        System.out.println(code);
        // 发短信
        R r = thirdParytFeignService.sendCode(phone, code);
        if (!Objects.equals(0, r.getCode())) {
            throw new BizException(BizExceptionEnum.AS_AUTH_REMOTESERVICE_FAIL,
                    String.format("server-name：gulimall-third-paryt，url：/sms/sendCode，param{phone：%s，code: %s}", phone, code), r);
        }

        redissonClient.getBucket(Constant.REDIS_SMS_CODE_PREFIX + phone).set(code, 3, TimeUnit.MINUTES);
        bucket.set(true, 60, TimeUnit.SECONDS);
        return R.ok();
    }

    @RequestMapping("/regist.html")
    public String regist(@Validated UserRegistVO userRegistVO, RedirectAttributes redirectAttributes) {
        RBucket<String> bucket = redissonClient.getBucket(Constant.REDIS_SMS_CODE_PREFIX + userRegistVO.getPhone());
        if (!bucket.isExists()) {
            redirectAttributes.addAttribute("data", "验证码已过期");
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        if (!Objects.equals(bucket.get(), userRegistVO.getCode())) {
            redirectAttributes.addAttribute("data", "验证码错误");
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        bucket.delete();

        R r = memberFeignService.saveUserInfo(userRegistVO);
        if (!Objects.equals(0, r.getCode())) {
            redirectAttributes.addAttribute("data", r.get("data"));
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object userInfo = session.getAttribute(Constant.REDIS_COOKIE_NAME);
        if (Objects.isNull(userInfo)) {
            return "login";
        }
        return "redirect:http://gulimall.com";
    }

    @PostMapping("/login")
    public String login(@Validated UserLoginVO userLoginVO, HttpSession session) {
        R r = memberFeignService.login(userLoginVO);
        session.setAttribute(Constant.REDIS_COOKIE_NAME, r.get("data"));
        if (!Objects.equals(0, r.getCode())) {
            return "redirect:http://auth.gulimall.com/login.html";
        }
        return "redirect:http://gulimall.com";
    }

}
