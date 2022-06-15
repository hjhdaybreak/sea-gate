package com.sea;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.constant.AdminConstants;
import com.sea.constant.NacosConstants;
import com.sea.pojo.dto.RegisterAppDTO;
import com.sea.pojo.dto.UnregisterAppDTO;
import com.sea.exception.SeaException;
import com.sea.utils.IpUtil;
import com.sea.utils.OkhttpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoRegisterListener implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AutoRegisterListener.class);

    private volatile AtomicBoolean registered = new AtomicBoolean(false);

    private final ClientConfigProperties properties;

    @NacosInjected
    private NamingService namingService;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    private final ExecutorService pool;

    private static final Gson gson = new GsonBuilder().create();

    private static List<String> ignoreUrlList = new LinkedList<>();

    static {
        ignoreUrlList.add("/error");
    }

    public AutoRegisterListener(ClientConfigProperties properties) {
        if (!check(properties)) {
            LOGGER.error("client config port,contextPath,appName and version can't be empty!");
            throw new SeaException("client config port,contextPath,appName and version can't be empty!");
        }
        this.properties = properties;
        pool = new ThreadPoolExecutor(1, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        doRegister();
        registerShutDownHook();
    }

    private void registerShutDownHook() {
        String url = "http://" + properties.getAdminUrl() + AdminConstants.UNREGISTER_PATH;
        UnregisterAppDTO unregisterAppDTO = new UnregisterAppDTO();
        unregisterAppDTO.setAppName(properties.getAppName());
        unregisterAppDTO.setVersion(properties.getVersion());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OkhttpTool.post(url, unregisterAppDTO);
            LOGGER.info(unregisterAppDTO.getAppName() + ":" + unregisterAppDTO.getVersion() + "unregister from sea-admin success!");
        }));
    }

    private void doRegister() {
        Instance instance = new Instance();
        instance.setIp(IpUtil.getLocalIpAddress());
        instance.setPort(properties.getPort());
        instance.setEphemeral(true);
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("version", properties.getVersion());
        metadataMap.put("appName", properties.getAppName());
        instance.setMetadata(metadataMap);
        try {
            namingService.registerInstance(properties.getAppName(), NacosConstants.APP_GROUP_NAME, instance);
        } catch (NacosException e) {
            LOGGER.error("register to nacos fail", e);
            throw new SeaException(e.getErrCode(), e.getErrMsg());
        }
        LOGGER.info("register interface info to nacos success!");
        // send register request to ship-admin
        String url = "http://" + properties.getAdminUrl() + AdminConstants.REGISTER_PATH;
        RegisterAppDTO registerAppDTO = buildRegisterAppDTO(instance);
        OkhttpTool.post(url, registerAppDTO);
        LOGGER.info("register to sea-admin success!");
    }

    private RegisterAppDTO buildRegisterAppDTO(Instance instance) {
        RegisterAppDTO registerAppDTO = new RegisterAppDTO();
        registerAppDTO.setAppName(properties.getAppName());
        registerAppDTO.setContextPath(properties.getContextPath());
        registerAppDTO.setVersion(properties.getVersion());

        registerAppDTO.setIp(instance.getIp());
        registerAppDTO.setPort(instance.getPort());

        return registerAppDTO;

    }

    private boolean check(ClientConfigProperties configProperties) {
        return configProperties.getPort() != null && configProperties.getContextPath() != null
                && configProperties.getVersion() != null && configProperties.getAppName() != null;
    }
}
