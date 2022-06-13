package com.sea.controller;

import com.sea.dto.RegisterAppDTO;
import com.sea.dto.UnregisterAppDTO;
import com.sea.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    @PostMapping("/register")
    public void register(@RequestBody @Validated RegisterAppDTO registerAppDTO) {
        appService.register(registerAppDTO);
    }

    @PostMapping("/unregister")
    public void unregister(@RequestBody UnregisterAppDTO unregisterAppDTO) {
        appService.unregister(unregisterAppDTO);
    }
}
