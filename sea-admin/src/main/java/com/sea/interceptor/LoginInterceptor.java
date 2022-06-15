package com.sea.interceptor;

import com.sea.constant.AdminConstants;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import com.sea.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new SeaException(SeaExceptionEnum.NOT_LOGIN);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AdminConstants.TOKEN_NAME)) {
                token = cookie.getValue();
            }
        }
        if (StringUtils.isEmpty(token)) {
            throw new SeaException(SeaExceptionEnum.NOT_LOGIN);
        }
        boolean result = JwtUtils.checkSignature(token);
        if (!result) {
            throw new SeaException(SeaExceptionEnum.TOKEN_ERROR);
        }
        return true;
    }
}
