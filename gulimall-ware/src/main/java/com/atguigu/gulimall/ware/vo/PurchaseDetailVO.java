package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PurchaseDetailVO {

    @NotNull(message = "采购需求ID不能为空")
    private Long itemId;

    @NotNull(message = "采购需求完成状态不能为空")
    private Integer status;

    private String reason;

}
