package com.sea.plugin.impl;

import com.sea.chain.PluginChain;
import com.sea.constant.SeaPluginEnum;
import com.sea.plugin.SeaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class DynamicRoutePlugin implements SeaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRoutePlugin.class);

    @Override
    public Integer order() {
        return SeaPluginEnum.DYNAMIC_ROUTE.getOrder();
    }

    @Override
    public String name() {
        return SeaPluginEnum.DYNAMIC_ROUTE.getName();
    }

    /**
     * @param exchange    当前请求和响应的上下文
     * @param pluginChain
     * @return
     */

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain pluginChain) {
        LOGGER.info("I am dynamic route");
        return pluginChain.execute(exchange, pluginChain);
    }
}
