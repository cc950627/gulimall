package com.atguigu.common.exception;

import com.atguigu.common.utils.R;

public class BizException extends RuntimeException {

    private Integer code;

    private String mdssage;

    private String detail;

    private R resaon;

    public BizException() {
    }

    public BizException(Integer code, String mdssage) {
        this.code = code;
        this.mdssage = mdssage;
    }

    public BizException(Integer code, String mdssage, String detail) {
        this.code = code;
        this.mdssage = mdssage;
        this.detail = detail;
    }

    public BizException(BizExceptionEnum bizExceptionEnum) {
        this.code = bizExceptionEnum.getCode();
        this.mdssage = bizExceptionEnum.getMessage();
    }

    public BizException(BizExceptionEnum bizExceptionEnum, String detail) {
        this.code = bizExceptionEnum.getCode();
        this.mdssage = bizExceptionEnum.getMessage();
        this.detail = detail;
    }

    public BizException(BizExceptionEnum bizExceptionEnum, String detail, R resaon) {
        this.code = bizExceptionEnum.getCode();
        this.mdssage = bizExceptionEnum.getMessage();
        this.detail = detail;
        this.resaon = resaon;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMdssage() {
        return mdssage;
    }

    public void setMdssage(String mdssage) {
        this.mdssage = mdssage;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public R getResaon() {
        return resaon;
    }

    public void setResaon(R resaon) {
        this.resaon = resaon;
    }
}
