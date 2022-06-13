package com.sea.constant;

public enum SeaExceptionEnum {
    PARAM_ERROR(1000, "参数错误");

    private Integer code;
    private String msg;

    SeaExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
