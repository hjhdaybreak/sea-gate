package com.sea.controller;

import com.sea.pojo.UserDTO;
import com.sea.pojo.vo.Result;
import com.sea.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("")
public class UserController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @PostMapping("/user")
    public Result add(@RequestBody @Validated UserDTO userDTO) {
        userService.add(userDTO);
        return Result.success();
    }


    @PostMapping("/user/login")
    public Result login(@RequestBody @Validated UserDTO userDTO, HttpServletResponse response) {
        userService.login(userDTO, response);
        return Result.success();
    }

    @GetMapping("/user/login/page")
    public String loginPage() {
        return "login";
    }

}
