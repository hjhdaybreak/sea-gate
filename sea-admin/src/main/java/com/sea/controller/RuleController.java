package com.sea.controller;

import com.sea.pojo.vo.AppRuleListVO;
import com.sea.pojo.vo.Result;
import com.sea.service.RuleService;
import com.sea.sync.WebsocketSyncCacheHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/rule")
public class RuleController {

    @Resource
    private RuleService ruleService;

    @Resource
    private WebsocketSyncCacheHandler handler;

    @GetMapping("/enabled")
    public Result<AppRuleListVO> getEnabledRule() {
        AppRuleListVO listVO = new AppRuleListVO();
        listVO.setList(ruleService.getEnabledRule());
        return Result.success(listVO);
    }


}
