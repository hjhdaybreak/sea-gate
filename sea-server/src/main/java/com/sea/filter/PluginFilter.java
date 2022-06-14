package com.sea.filter;

import com.sea.chain.PluginChain;
import com.sea.config.ServerConfigProperties;
import com.sea.plugin.DynamicRoutePlugin;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class PluginFilter implements WebFilter {

    private ServerConfigProperties properties;

    public PluginFilter(ServerConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        PluginChain pluginChain = new PluginChain();
        pluginChain.addPlugin(new DynamicRoutePlugin(properties));

        return pluginChain.execute(serverWebExchange, pluginChain);

    }
}
