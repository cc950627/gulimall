package com.atguigu.gulimall.ware.constant;

public enum PurchaseStatusEnum {

    CREATE(0, "新建"),

    ASSIGNED(1, "已分配"),

    RECEIVE(2, "已领取"),

    FINISH(3, "已完成"),

    HASERROR(4, "有异常");

    private Integer status;

    private String msg;

    PurchaseStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
