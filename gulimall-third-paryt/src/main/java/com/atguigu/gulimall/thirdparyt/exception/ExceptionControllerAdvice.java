package com.atguigu.gulimall.thirdparyt.exception;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.thirdparyt.controller")
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        Map<String, String> map = e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        log.error(JSON.toJSONString(map));
        return R.error(BizExceptionEnum.P_REQUEST_PARAM_ERROR.getCode(), BizExceptionEnum.P_REQUEST_PARAM_ERROR.getMessage()).put("data", map);
    }

    @ExceptionHandler(value = BizException.class)
    public R handleBizException(BizException e) {
        log.error(String.valueOf(e.getCode()),e.getMessage(), e.getDetail(), e.getResaon());
        return R.error(e.getCode(), e.getMdssage()).put("data", e.getDetail()).put("resaon", e.getResaon());
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error(e.getMessage());
        return R.error();
    }
}
