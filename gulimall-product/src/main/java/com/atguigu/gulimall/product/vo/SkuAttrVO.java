package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuAttrVO {

    /**
     * 属性id
     */
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;

    private List<SkuIdAttrValueVO> skuIdAttrValues;
}
