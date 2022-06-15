package com.sea.constant;

import java.util.stream.Stream;

public enum MatchMethodEnum {
    /**
     * =
     */
    EQUAL((byte) 1, "="),
    /**
     * regex
     */
    REGEX((byte) 2, "regex"),
    /**
     * like
     */
    LIKE((byte) 3, "like");


    private Byte code;

    private String desc;

    MatchMethodEnum(Byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MatchMethodEnum getByCode(Byte code) {
        return Stream.of(values()).filter(r -> r.getCode().equals(code)).findFirst().orElse(null);
    }
}
