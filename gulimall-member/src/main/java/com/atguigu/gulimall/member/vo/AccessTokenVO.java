package com.atguigu.gulimall.member.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessTokenVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessToken;

    private Long expiresIn;

    private String uid;

    private String isRealName;

}
