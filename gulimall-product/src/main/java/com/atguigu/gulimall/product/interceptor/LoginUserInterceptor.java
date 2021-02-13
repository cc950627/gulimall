package com.atguigu.gulimall.product.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.UserInfoTO;
import com.atguigu.common.utils.Constant;
import com.google.common.collect.Lists;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    // 需要放行的api
    private static final List<String> UIRS = Lists.newArrayList(
            "/**",
            "/index/**",
            "/product/spuinfo/test/**");

    public static ThreadLocal<UserInfoTO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        if (UIRS.stream().anyMatch(e -> antPathMatcher.match(e, uri))) {
            return true;
        }

        Object object = request.getSession().getAttribute(Constant.REDIS_COOKIE_NAME);
        if (Objects.isNull(object)) {
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        UserInfoTO userInfoTO = JSON.parseObject(JSON.toJSONString(object), UserInfoTO.class);
        loginUser.set(userInfoTO);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        loginUser.remove();
    }

}
