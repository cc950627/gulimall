package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVO {

    private String order;

    private List<OrderItemVO> orderItems;

}
