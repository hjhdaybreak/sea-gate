package com.sea.controller;

import com.sea.pojo.UpdateWeightDTO;
import com.sea.pojo.vo.InstanceVO;
import com.sea.pojo.vo.Result;
import com.sea.service.AppInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/app/instance")
public class AppInstanceController {

    @Autowired
    private AppInstanceService instanceService;

    @GetMapping("/list")
    public String list(@RequestParam("appId") Integer appId, ModelMap map) {
        List<InstanceVO> instanceVOS = instanceService.queryList(appId);
        map.put("instanceVOS", instanceVOS);
        return "instance";
    }

    @ResponseBody
    @PutMapping("")
    public Result updateWeight(@RequestBody @Validated UpdateWeightDTO updateWeightDTO) {
        instanceService.updateWeight(updateWeightDTO);
        return Result.success();
    }

}
