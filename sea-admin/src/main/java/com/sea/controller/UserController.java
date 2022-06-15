package com.sea.controller;

import com.sea.constant.AdminConstants;
import com.sea.pojo.UserDTO;
import com.sea.pojo.vo.Result;
import com.sea.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @PostMapping("")
    public Result add(@RequestBody @Validated UserDTO userDTO) {
        userService.add(userDTO);
        return Result.success();
    }

    @PostMapping("/login")
    public void login(@Validated UserDTO userDTO, HttpServletResponse response) throws IOException {
        userService.login(userDTO, response);
        response.sendRedirect("/app/list");
    }

    @GetMapping("/login/page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie(AdminConstants.TOKEN_NAME, null);
        cookie.setMaxAge(0); // 如果设置为0,则立即删除该Cookie;如果设置为负值的话,则为浏览器进程Cookie(内存中保存),关闭浏览器就失效
        cookie.setPath("/");
        response.addCookie(cookie);
        return "login";
    }
}
