package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sea.bean.User;
import com.sea.constant.AdminConstants;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import com.sea.mapper.UserMapper;
import com.sea.pojo.PayLoad;
import com.sea.pojo.UserDTO;
import com.sea.service.UserService;
import com.sea.utils.JwtUtils;
import com.sea.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {


    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Value("${sea.user-password-salt}")
    private String salt;

    @Override
    public void add(UserDTO userDTO) {

        User oldOne = queryByName(userDTO.getUserName());

        if (oldOne != null) {
            throw new SeaException("the userName already exist");
        }
        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setPassword(StringTools.md5Digest(userDTO.getPassword(), salt));
        user.setCreatedTime(LocalDateTime.now());
        userMapper.insert(user);


    }

    @Override
    public void login(UserDTO userDTO, HttpServletResponse response) {

        User user = queryByName(userDTO.getUserName());
        if (user == null) {
            throw new SeaException(SeaExceptionEnum.LOGIN_ERROR);
        }
        String pwd = StringTools.md5Digest(userDTO.getPassword(), salt);
        if (!pwd.equals(user.getPassword())) {
            throw new SeaException(SeaExceptionEnum.LOGIN_ERROR);
        }

        PayLoad payLoad = new PayLoad(user.getId(), user.getUserName());
        try {
            String token = JwtUtils.generateToken(payLoad);
            Cookie cookie = new Cookie(AdminConstants.TOKEN_NAME, token);
            cookie.setHttpOnly(true); // 防止Xss
            // 30 min
            cookie.setMaxAge(30 * 60);
            response.addCookie(cookie);
        } catch (Exception e) {
            LOGGER.error("login error", e);
        }
    }

    private User queryByName(String userName) {
        QueryWrapper<User> wrapper = Wrappers.query();
        wrapper.lambda().eq(User::getUserName, userName);
        return userMapper.selectOne(wrapper);
    }
}
