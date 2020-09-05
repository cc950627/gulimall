package com.atguigu.gulimall.ware.constant;

public enum WareLockStatusEnum {

    ISLOCK(1, "已锁定"),

    ISUNLOCK(2, "已解锁"),

    DEDUCTION(3, "扣减");

    private Integer status;

    private String msg;

    WareLockStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
