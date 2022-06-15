package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sea.event.RuleAddEvent;
import com.sea.event.RuleDeleteEvent;
import com.sea.bean.App;
import com.sea.bean.RouteRule;
import com.sea.constant.EnabledEnum;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import com.sea.mapper.AppMapper;
import com.sea.mapper.RouteRuleMapper;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.service.RuleService;
import com.sea.transfer.AppRuleVOTransfer;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RuleServiceImpl implements RuleService {

    @Resource
    private AppMapper appMapper;

    @Resource
    private RouteRuleMapper ruleMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;


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


        List<AppRuleDTO> appRuleDTOS = AppRuleVOTransfer.INSTANCE.mapToVOList(routeRules);

        Map<Integer, String> nameMap = new HashMap<>();
        for (App app : apps) {
            if (nameMap.put(app.getId(), app.getAppName()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        for (AppRuleDTO r : appRuleDTOS) {
            r.setAppName(nameMap.get(r.getAppId()));
        }
        return appRuleDTOS;
    }

    @Override
    public void add(AppRuleDTO appRuleDTO) {
        RouteRule routeRule = new RouteRule();
        BeanUtils.copyProperties(appRuleDTO, routeRule);
        routeRule.setEnabled(EnabledEnum.ENABLE.getCode());
        routeRule.setCreatedTime(LocalDateTime.now());
        ruleMapper.insert(routeRule);
        appRuleDTO.setId(routeRule.getId());
        eventPublisher.publishEvent(new RuleAddEvent(this, appRuleDTO));
    }

    @Override
    public void delete(Integer id) {
        RouteRule routeRule = ruleMapper.selectById(id);
        if (routeRule == null) {
            throw new SeaException(SeaExceptionEnum.PARAM_ERROR);
        }
        AppRuleDTO appRuleDTO = new AppRuleDTO();
        BeanUtils.copyProperties(routeRule, appRuleDTO);
        App app = appMapper.selectById(appRuleDTO.getAppId());
        appRuleDTO.setAppName(app.getAppName());

        ruleMapper.deleteById(id);
        eventPublisher.publishEvent(new RuleDeleteEvent(this, appRuleDTO));
    }
}
