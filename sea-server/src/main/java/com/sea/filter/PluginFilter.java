package com.sea.filter;

import com.sea.chain.PluginChain;
import com.sea.plugin.impl.DynamicRoutePlugin;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class PluginFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        PluginChain pluginChain = new PluginChain();

        pluginChain.addPlugin(new DynamicRoutePlugin());

        return pluginChain.execute(serverWebExchange, pluginChain);
    }
}
