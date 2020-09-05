package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class CatelogShowVO {

    private String catelog1Id;
    private List<CatelogShowVO> childes;
    private String id;
    private String name;
}
