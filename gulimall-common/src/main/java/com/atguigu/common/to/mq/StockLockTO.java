package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockTO {

    private Long id;

    private List<StockLockDetailTO> stockLockDetails;
}
