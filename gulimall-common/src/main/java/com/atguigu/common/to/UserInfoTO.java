package com.atguigu.common.to;

import lombok.Data;

@Data
public class UserInfoTO {

    private Long id;

    private String name;

    private String nickname;

    private String gender;

    private String profileImageUrl;

    private String socialUid;

    private String accessToken;

    private String expiresIn;

    private Integer integration;

}
