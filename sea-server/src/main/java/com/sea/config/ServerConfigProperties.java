package com.sea.config;

import com.sea.constant.LoadBalanceConstants;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sea.gate")
public class ServerConfigProperties implements InitializingBean {

    private String loadBalance = LoadBalanceConstants.ROUND;

    private Long timeOutMillis = 3000L;

    /**
     * 缓存刷新间隔,默认10s
     */
    private Long cacheRefreshInterval = 10L;


    private Integer webSocketPort;

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Long getTimeOutMillis() {
        return timeOutMillis;
    }

    public void setTimeOutMillis(Long timeOutMillis) {
        this.timeOutMillis = timeOutMillis;
    }

    public Long getCacheRefreshInterval() {
        return cacheRefreshInterval;
    }

    public void setCacheRefreshInterval(Long cacheRefreshInterval) {
        this.cacheRefreshInterval = cacheRefreshInterval;
    }

    public Integer getWebSocketPort() {
        return webSocketPort;
    }

    public void setWebSocketPort(Integer webSocketPort) {
        this.webSocketPort = webSocketPort;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.webSocketPort == null || this.webSocketPort <= 0) {
            throw new SeaException(SeaExceptionEnum.CONFIG_ERROR);
        }
    }
}
