package com.sea.controller;

import com.sea.pojo.dto.AppRuleDTO;
import com.sea.pojo.vo.AppRuleListVO;
import com.sea.pojo.vo.Result;
import com.sea.service.RuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/rule")
public class RuleController {

    @Resource
    private RuleService ruleService;

    @GetMapping("/enabled")
    public Result<AppRuleListVO> getEnabledRule() {
        AppRuleListVO listVO = new AppRuleListVO();
        listVO.setList(ruleService.getEnabledRule());
        return Result.success(listVO);
    }

    @PostMapping("")
    public Result add(@RequestBody @Validated AppRuleDTO appRuleDTO) {
        ruleService.add(appRuleDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        ruleService.delete(id);
        return Result.success();
    }


}
