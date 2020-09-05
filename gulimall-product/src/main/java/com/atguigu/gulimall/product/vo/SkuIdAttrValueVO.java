package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuIdAttrValueVO {

    private String attrValue;

    private List<Long> skuIds;
}
