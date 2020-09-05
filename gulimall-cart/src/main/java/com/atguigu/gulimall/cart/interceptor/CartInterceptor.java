package com.atguigu.gulimall.cart.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.util.UuidUtils;
import com.atguigu.common.to.UserInfoTO;
import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.cart.constant.CartConstant;
import com.atguigu.gulimall.cart.to.CartUserInfoTO;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<CartUserInfoTO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        CartUserInfoTO cartUserInfoTO = new CartUserInfoTO();

        HttpSession session = request.getSession();
        Object object = session.getAttribute(Constant.REDIS_COOKIE_NAME);
        Optional.ofNullable(object).ifPresent(e -> cartUserInfoTO.setUserId(JSON.parseObject(JSON.toJSONString(object), UserInfoTO.class).getId()));

        Cookie[] cookies = request.getCookies();
        Optional.ofNullable(cookies).flatMap(e -> Stream.of(cookies).filter(o -> Objects.equals(o.getName(),
                CartConstant.TEMP_USER_COOKIE_NAME)).findAny()).ifPresent(o -> cartUserInfoTO.setUserKey(o.getValue()));

        String userKey = Optional.ofNullable(cartUserInfoTO.getUserKey()).orElseGet(UuidUtils::generateUuid);
        cartUserInfoTO.setUserKey(userKey);

        threadLocal.set(cartUserInfoTO);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isEmpty(cookies) || Stream.of(cookies).noneMatch(e -> Objects.equals(e.getName(), CartConstant.TEMP_USER_COOKIE_NAME))) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, threadLocal.get().getUserKey());
            cookie.setDomain(Constant.REDIS_DOMAIN_NAME);
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

}
