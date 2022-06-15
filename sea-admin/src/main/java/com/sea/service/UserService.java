package com.sea.service;

import com.sea.pojo.UserDTO;

import javax.servlet.http.HttpServletResponse;

public interface UserService {

    void add(UserDTO userDTO);

    void login(UserDTO userDTO, HttpServletResponse response);

}
