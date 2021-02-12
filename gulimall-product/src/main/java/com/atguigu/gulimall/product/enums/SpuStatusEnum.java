package com.atguigu.gulimall.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum SpuStatusEnum {

    /**
     * 新建
     */
    SPU_NEW(0, "新建"),

    /**
     * 上架
     */
    SPU_UP(1, "上架"),

    /**
     * 下架
     */
    SPU_DOWN(2, "下架"),;

    @EnumValue
    private int status;

    private String type;

    SpuStatusEnum(int status, String type) {
        this.status = status;
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
