package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.bean.AppPlugin;
import com.sea.bean.Plugin;
import com.sea.constant.EnabledEnum;
import com.sea.bean.App;
import com.sea.bean.AppInstance;
import com.sea.pojo.AppPluginDTO;
import com.sea.pojo.dto.AppInfoDTO;
import com.sea.pojo.dto.RegisterAppDTO;
import com.sea.pojo.dto.ServiceInstance;
import com.sea.pojo.dto.UnregisterAppDTO;
import com.sea.exception.SeaException;
import com.sea.mapper.AppInstanceMapper;
import com.sea.mapper.AppMapper;
import com.sea.mapper.AppPluginMapper;
import com.sea.mapper.PluginMapper;
import com.sea.service.AppService;
import com.sea.transfer.AppInstanceTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class AppServiceImpl implements AppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class);

    @Resource
    private AppMapper appMapper;

    @Resource
    private AppInstanceMapper instanceMapper;


    @Resource
    private PluginMapper pluginMapper;

    @Resource
    private AppPluginMapper appPluginMapper;


    private final Gson gson = new GsonBuilder().create();

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterAppDTO dto) {
        App app = queryByAppName(dto.getAppName());
        Integer appId;
        if (app == null) {
            appId = addApp(dto);
        } else {
            appId = app.getId();
        }
        AppInstance instance = query(appId, dto.getVersion(), dto.getIp(), dto.getPort());
        if (instance == null) {
            AppInstance appInstance = new AppInstance();
            appInstance.setAppId(appId);
            appInstance.setVersion(dto.getVersion());
            appInstance.setIp(dto.getIp());
            appInstance.setPort(dto.getPort());
            appInstance.setCreatedTime(LocalDateTime.now());
            instanceMapper.insert(appInstance);
        }
        LOGGER.info("register app success,dto:[{}]", gson.toJson(dto));
    }

    private AppInstance query(Integer appId, String version, String ip, Integer port) {
        QueryWrapper<AppInstance> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppInstance::getAppId, appId)
                .eq(AppInstance::getVersion, version)
                .eq(AppInstance::getIp, ip)
                .eq(AppInstance::getPort, port);
        return instanceMapper.selectOne(wrapper);
    }


    private Integer addApp(RegisterAppDTO dto) {
        App app = new App();
        BeanUtils.copyProperties(dto, app);
        app.setEnabled(EnabledEnum.ENABLE.getCode());
        app.setCreatedTime(LocalDateTime.now());
        appMapper.insert(app);
        bindPlugins(app);
        return app.getId();
    }

    private void bindPlugins(App app) {
        List<Plugin> plugins = pluginMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(plugins)) {
            throw new SeaException("must init plugins first!");
        }
        plugins.forEach(p -> {
            AppPlugin appPlugin = new AppPlugin();
            appPlugin.setAppId(app.getId());
            appPlugin.setPluginId(p.getId());
            appPlugin.setEnabled(EnabledEnum.ENABLE.getCode());
            appPluginMapper.insert(appPlugin);
        });
    }

    @Override
    public void unregister(UnregisterAppDTO dto) {
        App app = queryByAppName(dto.getAppName());
        if (app == null) {
            return;
        }
        QueryWrapper<AppInstance> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppInstance::getAppId, app.getId())
                .eq(AppInstance::getVersion, dto.getVersion())
                .eq(AppInstance::getIp, dto.getIp())
                .eq(AppInstance::getPort, dto.getPort());
        instanceMapper.delete(wrapper);
        LOGGER.info("unregister app instance success,dto:[{}]", gson.toJson(dto));
    }

    @Override
    public List<AppInfoDTO> getAppInfos(List<String> appNames) {
        List<App> apps = getAppList(appNames);
        List<Integer> appIds = new ArrayList<>();
        for (App app : apps) {
            Integer id = app.getId();
            appIds.add(id);
        }

        QueryWrapper<AppInstance> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(AppInstance::getAppId, appIds);
        List<AppInstance> appInstances = instanceMapper.selectList(wrapper);
        List<AppPluginDTO> appPluginDTOS = appPluginMapper.queryEnabledPlugins(appIds);

        if (CollectionUtils.isEmpty(appInstances) || CollectionUtils.isEmpty(appPluginDTOS)) {
            LOGGER.info("no app infos....");
            return Lists.newArrayList();
        }
        return buildAppInfos(apps, appInstances, appPluginDTOS);
    }

    private List<AppInfoDTO> buildAppInfos(List<App> apps, List<AppInstance> appInstances, List<AppPluginDTO> appPluginDTOS) {
        List<AppInfoDTO> list = Lists.newArrayList();
        for (App app : apps) {
            AppInfoDTO appInfoDTO = new AppInfoDTO();
            appInfoDTO.setAppId(app.getId());
            appInfoDTO.setAppName(app.getAppName());
            List<String> enabledPlugins = new ArrayList<>();
            for (AppPluginDTO appPluginDTO : appPluginDTOS) {
                if (appPluginDTO.getAppId().equals(app.getId())) {
                    String code = appPluginDTO.getCode();
                    enabledPlugins.add(code);
                }
            }
            appInfoDTO.setEnabledPlugins(enabledPlugins);
            List<AppInstance> instances = new ArrayList<>();
            for (AppInstance r : appInstances) {
                if (r.getAppId().equals(app.getId())) {
                    instances.add(r);
                }
            }

            List<ServiceInstance> serviceList = AppInstanceTransfer.INSTANCE.mapToServiceList(instances);
            appInfoDTO.setInstances(serviceList);
            list.add(appInfoDTO);
        }
        return list;
    }

    private List<App> getAppList(List<String> appNames) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(App::getAppName, appNames);
        return appMapper.selectList(wrapper);
    }

    private App queryByAppName(String appName) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(App::getAppName, appName);
        return appMapper.selectOne(wrapper);
    }
}
