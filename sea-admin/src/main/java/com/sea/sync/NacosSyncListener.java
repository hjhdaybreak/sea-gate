package com.sea.sync;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.constant.NacosConstants;
import com.sea.pojo.NacosMetadata;
import com.sea.pojo.dto.AppInfoDTO;
import com.sea.pojo.dto.ServiceInstance;
import com.sea.service.AppService;
import com.sea.utils.OkhttpTool;
import com.sea.utils.SeaThreadFactory;
import com.sea.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据同步
 * app数据同步
 * <p>
 * 后台服务（如订单服务）启动时，只将服务名，版本，ip地址和端口号注册到了Nacos，并没有实例的权重和启用的插件信息怎么办？
 * <p>
 * 一般在线的实例权重和插件列表都是在管理界面配置，然后动态生效的，所以需要ship-admin定时更新实例的权重和插件信息
 */
@Configuration
public class NacosSyncListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosSyncListener.class);

    private static ScheduledThreadPoolExecutor scheduledPool = new ScheduledThreadPoolExecutor(1,
            new SeaThreadFactory("nacos-sync", true).create());

    @NacosInjected
    private NamingService namingService;

    @Value("${nacos.discovery.server-addr}")
    private String baseUrl;

    @Resource
    private AppService appService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        String url = "http://" + baseUrl + NacosConstants.INSTANCE_UPDATE_PATH;
        scheduledPool.scheduleWithFixedDelay(new NacosSyncTask(namingService, url, appService), 0, 30L, TimeUnit.SECONDS);
    }

    class NacosSyncTask implements Runnable {

        private NamingService namingService;

        private String url;

        private AppService appService;

        private Gson gson = new GsonBuilder().create();

        public NacosSyncTask(NamingService namingService, String url, AppService appService) {
            this.namingService = namingService;
            this.url = url;
            this.appService = appService;
        }

        @Override
        public void run() {
            try {
                ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, NacosConstants.APP_GROUP_NAME);
                if (CollectionUtils.isEmpty(services.getData())) {
                    return;
                }
                List<String> appNames = services.getData();
                List<AppInfoDTO> appInfos = appService.getAppInfos(appNames);
                for (AppInfoDTO appInfo : appInfos) {
                    if (CollectionUtils.isEmpty(appInfo.getInstances())) {
                        continue;
                    }
                    for (ServiceInstance instance : appInfo.getInstances()) {
                        Map<String, Object> queryMap = buildQueryMap(appInfo, instance);
                        String resp = OkhttpTool.doPut(url, queryMap, "");
                        LOGGER.info("response :{}", resp);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("nacos sync task error", e);
            }
        }

        private Map<String, Object> buildQueryMap(AppInfoDTO appInfo, ServiceInstance instance) {
            Map<String, Object> map = new HashMap<>();
            map.put("serviceName", appInfo.getAppName());
            map.put("groupName", NacosConstants.APP_GROUP_NAME);
            map.put("ip", instance.getIp());
            map.put("port", instance.getPort());
            map.put("weight", instance.getWeight().doubleValue());
            NacosMetadata metadata = new NacosMetadata();

            metadata.setAppName(appInfo.getAppName());
            metadata.setVersion(instance.getVersion());
            metadata.setPlugins(String.join(",", appInfo.getEnabledPlugins()));

            map.put("metadata", StringTools.urlEncode(gson.toJson(metadata)));
            map.put("ephemeral", true);
            return map;
        }
    }

}
