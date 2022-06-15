package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.sea.bean.AppInstance;
import com.sea.constant.MatchMethodEnum;
import com.sea.constant.MatchObjectEnum;
import com.sea.event.RuleAddEvent;
import com.sea.event.RuleDeleteEvent;
import com.sea.bean.App;
import com.sea.bean.RouteRule;
import com.sea.constant.EnabledEnum;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import com.sea.mapper.AppInstanceMapper;
import com.sea.mapper.AppMapper;
import com.sea.mapper.RouteRuleMapper;
import com.sea.pojo.ChangeStatusDTO;
import com.sea.pojo.RuleDTO;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.pojo.vo.RuleVO;
import com.sea.service.RuleService;
import com.sea.transfer.AppRuleVOTransfer;
import com.sea.transfer.RuleVOTransfer;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleServiceImpl implements RuleService {

    @Resource
    private AppMapper appMapper;

    @Resource
    private RouteRuleMapper ruleMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;


    @Resource
    private AppInstanceMapper instanceMapper;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RuleDTO ruleDTO) {
        checkRule(ruleDTO);

        App app = appMapper.selectById(ruleDTO.getAppId());
        RouteRule routeRule = new RouteRule();
        BeanUtils.copyProperties(ruleDTO, routeRule);
        routeRule.setCreatedTime(LocalDateTime.now());
        ruleMapper.insert(routeRule);

        if (EnabledEnum.ENABLE.getCode().equals(ruleDTO.getEnabled())) {
            AppRuleDTO appRuleDTO = new AppRuleDTO();
            BeanUtils.copyProperties(routeRule, appRuleDTO);
            appRuleDTO.setAppName(app.getAppName());
            eventPublisher.publishEvent(new RuleAddEvent(this, appRuleDTO));
        }
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public List<RuleVO> queryList(String appName) {
        Integer appId = null;
        if (!StringUtils.isEmpty(appName)) {
            App app = queryByAppName(appName);
            if (app == null) {
                return Lists.newArrayList();
            }
            appId = app.getId();
        }
        QueryWrapper<RouteRule> query = Wrappers.query();
        query.lambda().eq(Objects.nonNull(appId), RouteRule::getAppId, appId)
                .orderByDesc(RouteRule::getCreatedTime);

        List<RouteRule> rules = ruleMapper.selectList(query);
        if (CollectionUtils.isEmpty(rules)) {
            return Lists.newArrayList();
        }

        List<RuleVO> ruleVOS = RuleVOTransfer.INSTANCE.mapToVOList(rules);
        List<Integer> list = new ArrayList<>();

        for (RuleVO r : ruleVOS) {
            Integer id = r.getAppId();
            list.add(id);
        }

        Map<Integer, String> nameMap = getAppNameMap(list);
        for (RuleVO ruleVO : ruleVOS) {
            ruleVO.setAppName(nameMap.get(ruleVO.getAppId()));
            ruleVO.setMatchStr(buildMatchStr(ruleVO));
        }
        return ruleVOS;
    }

    private String buildMatchStr(RuleVO ruleVO) {
        if (MatchObjectEnum.DEFAULT.getCode().equals(ruleVO.getMatchObject())) {
            return ruleVO.getMatchObject();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(ruleVO.getMatchKey()).append("] ");
            sb.append(MatchMethodEnum.getByCode(ruleVO.getMatchMethod()).getDesc());
            sb.append(" [").append(ruleVO.getMatchRule()).append("]");
            return sb.toString();
        }
    }


    private Map<Integer, String> getAppNameMap(List<Integer> appIdList) {
        QueryWrapper<App> query = Wrappers.query();
        query.lambda().in(App::getId, appIdList);
        List<App> apps = appMapper.selectList(query);
        return apps.stream().collect(Collectors.toMap(App::getId, App::getAppName));
    }

    private App queryByAppName(String appName) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(App::getAppName, appName);
        App app = appMapper.selectOne(wrapper);
        return app;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeStatus(ChangeStatusDTO statusDTO) {
        RouteRule routeRule = new RouteRule();
        routeRule.setId(statusDTO.getId());
        routeRule.setEnabled(statusDTO.getEnabled());
        ruleMapper.updateById(routeRule);
        AppRuleDTO appRuleDTO = new AppRuleDTO();
        BeanUtils.copyProperties(routeRule, appRuleDTO);
        appRuleDTO.setAppName(statusDTO.getAppName());
        if (EnabledEnum.ENABLE.getCode().equals(statusDTO.getEnabled())) {
            eventPublisher.publishEvent(new RuleAddEvent(this, appRuleDTO));
        } else {
            eventPublisher.publishEvent(new RuleDeleteEvent(this, appRuleDTO));
        }
    }

    private void checkRule(RuleDTO ruleDTO) {
        QueryWrapper<RouteRule> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RouteRule::getName, ruleDTO.getName());
        if (!CollectionUtils.isEmpty(ruleMapper.selectList(wrapper))) {
            throw new SeaException("规则名称不能重复");
        }
        if (MatchObjectEnum.DEFAULT.getCode().equals(ruleDTO.getMatchObject())) {
            ruleDTO.setMatchKey(null);
            ruleDTO.setMatchMethod(null);
            ruleDTO.setMatchRule(null);
        } else {
            if (StringUtils.isEmpty(ruleDTO.getMatchKey()) || ruleDTO.getMatchMethod() == null
                    || StringUtils.isEmpty(ruleDTO.getMatchRule())) {
                throw new SeaException(SeaExceptionEnum.PARAM_ERROR);
            }
        }

        // check version
        QueryWrapper<AppInstance> query = Wrappers.query();
        query.lambda().eq(AppInstance::getAppId, ruleDTO.getAppId())
                .eq(AppInstance::getVersion, ruleDTO.getVersion());
        List<AppInstance> list = instanceMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            throw new SeaException("实例版本不存在");
        }
    }
}
