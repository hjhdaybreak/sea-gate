package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.constant.EnabledEnum;
import com.sea.bean.App;
import com.sea.bean.AppInstance;
import com.sea.dto.RegisterAppDTO;
import com.sea.dto.UnregisterAppDTO;
import com.sea.mapper.AppInstanceMapper;
import com.sea.mapper.AppMapper;
import com.sea.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class AppServiceImpl implements AppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class);

    @Resource
    private AppMapper appMapper;

    @Resource
    private AppInstanceMapper appInstanceMapper;

    private final Gson gson = new GsonBuilder().create();

    @Override
    public void register(RegisterAppDTO dto) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(App::getAppName, dto.getAppName());
        App app = appMapper.selectOne(wrapper);
        if (app == null) {
            // first register
            app = new App();
            BeanUtils.copyProperties(dto, app);
            app.setEnabled(EnabledEnum.ENABLE.getCode());
            app.setCreatedTime(LocalDateTime.now());
            appMapper.insert(app);
        }
        AppInstance appInstance = new AppInstance();
        appInstance.setAppId(app.getId());
        appInstance.setVersion(dto.getVersion());
        appInstance.setIp(dto.getIp());
        appInstance.setPort(dto.getPort());
        appInstance.setCreatedTime(LocalDateTime.now());
        appInstanceMapper.insert(appInstance);
        LOGGER.info("register app success,dto:[{}]", gson.toJson(dto));
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
        appInstanceMapper.delete(wrapper);
        LOGGER.info("unregister app instance success,dto:[{}]", gson.toJson(dto));



    }

    private App queryByAppName(String appName) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(App::getAppName, appName);
        return appMapper.selectOne(wrapper);
    }
}
