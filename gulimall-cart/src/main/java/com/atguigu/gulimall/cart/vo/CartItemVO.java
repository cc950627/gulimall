package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItemVO {

    private Long skuId;

    private Boolean check;

    private String skuTitle;

    private String skuDefaultImg;

    private List<String> skuAttrs;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

}
