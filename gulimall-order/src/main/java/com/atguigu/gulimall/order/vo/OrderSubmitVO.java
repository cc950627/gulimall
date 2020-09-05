package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVO {

    private Long addrId;

    private Integer payType;

    private String orderToken;

    private BigDecimal payPrice;
}
