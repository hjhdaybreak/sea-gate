package com.sea.pojo.dto;

import java.util.List;

public class AppInfoDTO {

    private Integer appId;

    private String appName;

    private List<ServiceInstance> instances;

    /**
     * TODO ??? 插件代码 code ?
     */
    private List<String> enabledPlugins;

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ServiceInstance> instances) {
        this.instances = instances;
    }

    public List<String> getEnabledPlugins() {
        return enabledPlugins;
    }

    public void setEnabledPlugins(List<String> enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
    }
}
