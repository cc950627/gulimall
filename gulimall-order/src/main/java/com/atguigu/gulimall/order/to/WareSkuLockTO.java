package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.vo.OrderItemVO;
import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockTO {

    private String orderSn;

    private List<OrderItemVO> orderItems;

}
