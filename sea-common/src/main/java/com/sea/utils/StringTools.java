package com.sea.utils;

import com.sea.constant.MatchMethodEnum;
import com.sea.exception.SeaException;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class StringTools {

    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * @param value       请求中的值
     * @param matchMethod 规则中的方法 EQUAL REGEX LIKE
     * @param matchRule   规则中的规则
     * @return
     */

    public static boolean match(String value, Byte matchMethod, String matchRule) {
        if (MatchMethodEnum.EQUAL.getCode().equals(matchMethod)) {
            return value.equals(matchRule);
        } else if (MatchMethodEnum.REGEX.getCode().equals(matchMethod)) {
            return Pattern.matches(matchRule, value);
        } else if (MatchMethodEnum.LIKE.getCode().equals(matchMethod)) {
            return value.contains(matchRule);
        } else {
            throw new SeaException("invalid matchMethod");
        }
    }

    public static String byteToStr(byte[] data) {
        try {
            return new String(data, CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
