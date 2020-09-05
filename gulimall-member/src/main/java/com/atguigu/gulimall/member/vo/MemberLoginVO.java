package com.atguigu.gulimall.member.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MemberLoginVO {

    private String loginacct;

    private String password;

}
