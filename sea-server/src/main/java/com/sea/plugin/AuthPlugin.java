package com.sea.plugin;

import com.sea.chain.PluginChain;
import com.sea.config.ServerConfigProperties;
import com.sea.constant.SeaPluginEnum;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AuthPlugin extends AbstractSeaPlugin {

    public AuthPlugin(ServerConfigProperties properties) {
        super(properties);
    }

    @Override
    public Integer order() {
        return SeaPluginEnum.AUTH.getOrder();
    }

    @Override
    public String name() {
        return SeaPluginEnum.AUTH.getName();
    }

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain pluginChain) {
        System.out.println("auth plugin");
        return pluginChain.execute(exchange, pluginChain);
    }
}
