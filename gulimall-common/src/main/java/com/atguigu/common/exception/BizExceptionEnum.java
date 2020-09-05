package com.atguigu.common.exception;

public enum BizExceptionEnum {

    P_REQUEST_PARAM_ERROR(10001, "请求参数错误"),

    P_REMOVE_ATTRGROUP_FAIL(10002, "删除属性分组失败"),

    P_GET_ATTRRELATION_FAIL(10003, "获取属性relation失败"),

    P_SAVE_ATTRRELATION_FAIL(10004, "保存属性relation失败"),

    P_GET_ATTRLIST_FAIL(10005, "获取属性列表失败"),

    P_REQ_REMOTESERVICE_FAIL(10006, "调用远程服务异常"),

    P_REQ_COVER_LIMIT_FLOW(10007, "当前请求被限流"),

    C_REQUEST_PARAM_ERROR(11001, "请求参数错误"),

    W_PURCHASE_MAGER_FAIL(12001, "采购需求合并失败"),

    W_PURCHASE_REMOVE_FAIL(12002, "采购订单删除失败"),

    W_PURCHASEDETAIL_REMOVE_FAIL(12003, "采购需求删除失败"),

    W_PURCHASE_RECEIVE_FAIL(12004, "采购订单领取失败"),

    W_REQ_REMOTESERVICE_FAIL(12005, "调用远程服务异常"),

    W_REQ_REMOTESEFINISH_FAIL(12006, "采购订单完成失败"),

    W_LOCK_STOCK_FAIL(12007, "库存锁定失败"),

    S_ES_OPERATION_ERROR(13001, "elasticsearch操作异常"),

    AS_AUTH_REMOTESERVICE_FAIL(14001, "调用远程服务异常"),

    AS_NOT_SEND_CODE(15001, "不允许发送失败"),

    TP_SEND_CODE_ERROR(15001, "发送验证码失败"),

    M_GET_DATA_ERROR(16001, "获取数据异常"),

    M_CHECKED_DATA_UNQUALIFIED(16002, "校验数据不通过"),

    M_REQ_REMOTESERVICE_FAIL(16003, "调用远程服务异常"),

    M_LOGIN_FAIL(16004, "登录失败"),

    S_PRODUCT_NOT_EXITS(17001, "秒杀商品不存在"),

    S_NOT_ACTIVITY_DATE(17002, "不在秒杀活动时间内"),

    S_RANDOMCODE_CHECK_FAIL(17003, "秒杀商品随机码验证失败"),

    S_PRODUCTINFO_CHECK_FAIL(17004, "秒杀商品信息验证失败"),

    S_PRODUCT_NUMBER_OUT_LIMIT(17005, "秒杀商品数量超出限制"),

    S_PRODCUT_ALREADY_BUY(17006, "当前用户已抢购过该商品"),

    S_PRODCUT_SOLD_EMPTY(17007, "秒杀商品已经售空"),

    S_REQUEST_OUT_LIMIT(17008, "系统繁忙请刷新页面重试"),

    S_REQ_COVER_LIMIT_FLOW(17009, "当前请求被限流"),

    G_REQ_COVER_LIMIT_FLOW(18001, "当前请求被限流");

    private Integer code;

    private String message;

    private BizExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
