package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserLoginVO {

    @NotBlank(message = "用户名不能为空")
    private String loginacct;

    @NotBlank(message = "密码不能为空")
    private String password;

}
