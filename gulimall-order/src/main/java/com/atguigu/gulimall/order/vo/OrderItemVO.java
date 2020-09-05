package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVO {

    private Long skuId;

    private String skuTitle;

    private String skuDefaultImg;

    private List<String> skuAttrs;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    private Boolean hasStock;
}
