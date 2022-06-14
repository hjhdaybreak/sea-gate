package com.sea.exception;

import com.sea.constant.SeaExceptionEnum;

public class SeaException extends RuntimeException {

    private Integer code;

    private String errMsg;

    public SeaException(String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }

    public SeaException(Integer code, String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

    public SeaException(SeaExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.code = exceptionEnum.getCode();
        this.errMsg = exceptionEnum.getMsg();
    }

    public Integer getCode() {
        return code;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
