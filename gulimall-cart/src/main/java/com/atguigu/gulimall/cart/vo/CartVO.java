package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {

    private List<CartItemVO> items;

    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce;

}
