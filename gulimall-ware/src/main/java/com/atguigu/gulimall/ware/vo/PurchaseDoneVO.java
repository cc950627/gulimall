package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVO {

    @NotNull(message = "采购单ID不能为空")
    private Long id;

    @NotEmpty(message = "采购需求不能为空")
    private List<PurchaseDetailVO> items;
}
