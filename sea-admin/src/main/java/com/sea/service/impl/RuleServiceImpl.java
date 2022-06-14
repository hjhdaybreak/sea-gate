package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sea.bean.App;
import com.sea.bean.RouteRule;
import com.sea.constant.EnabledEnum;
import com.sea.mapper.AppMapper;
import com.sea.mapper.RouteRuleMapper;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.service.RuleService;
import com.sea.transfer.AppRuleVOTransfer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RuleServiceImpl implements RuleService {

    @Resource
    private AppMapper appMapper;

    @Resource
    private RouteRuleMapper ruleMapper;

    @Override
    public List<AppRuleDTO> getEnabledRule() {

        //获取所有应用
        QueryWrapper<App> wrapper = Wrappers.query();
        wrapper.lambda().eq(App::getEnabled, EnabledEnum.ENABLE.getCode());
        List<App> apps = appMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(apps)) {
            return new ArrayList<>();
        }

        List<Integer> appIds = new ArrayList<>();
        for (App app : apps) {
            Integer id = app.getId();
            appIds.add(id);
        }

        //获取对应的路由规则
        QueryWrapper<RouteRule> query = Wrappers.query();
        query.lambda().in(RouteRule::getAppId, appIds)
                .eq(RouteRule::getEnabled, EnabledEnum.ENABLE.getCode());
        List<RouteRule> routeRules = ruleMapper.selectList(query);

        //转 VO
        List<AppRuleDTO> appRuleVOS = AppRuleVOTransfer.INSTANCE.mapToVOList(routeRules);

        Map<Integer, String> nameMap = new HashMap<>();
        for (App app : apps) {
            if (nameMap.put(app.getId(), app.getAppName()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        for (AppRuleDTO r : appRuleVOS) {
            r.setAppName(nameMap.get(r.getAppId()));
        }
        return appRuleVOS;
    }
}
