package com.sea.constant;

public enum MatchMethodEnum {

    EQUAL(1, "="),
    REGEX(2, "regex"),
    LIKE(3, "like");

    private Integer code;

    private String desc;

    MatchMethodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
