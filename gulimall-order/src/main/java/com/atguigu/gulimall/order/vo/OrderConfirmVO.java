package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVO {

    private List<MemberAddressVO> memberAddress;

    private List<OrderItemVO> orderItems;

    private Integer integration;

    private BigDecimal total;

    private BigDecimal payPrice;

    private Integer count;

    private String orderToken;
}
