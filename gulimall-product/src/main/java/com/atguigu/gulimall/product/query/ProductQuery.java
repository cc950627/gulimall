package com.atguigu.gulimall.product.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductQuery implements Serializable {

    private String key;

    private Long catelogId;
}
