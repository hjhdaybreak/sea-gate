package com.sea.filter;

import com.sea.cache.ServiceCache;
import com.sea.chain.PluginChain;
import com.sea.config.ServerConfigProperties;
import com.sea.constant.SeaExceptionEnum;
import com.sea.exception.SeaException;
import com.sea.plugin.AuthPlugin;
import com.sea.plugin.DynamicRoutePlugin;
import org.springframework.http.server.RequestPath;
import org.springframework.util.CollectionUtils;
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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String appName = parseAppName(exchange);
        if (CollectionUtils.isEmpty(ServiceCache.getAllInstances(appName))) {
            throw new SeaException(SeaExceptionEnum.SERVICE_NOT_FIND);
        }

        PluginChain pluginChain = new PluginChain(properties,appName);
        pluginChain.addPlugin(new DynamicRoutePlugin(properties));
        pluginChain.addPlugin(new AuthPlugin(properties));
        return pluginChain.execute(exchange, pluginChain);

    }

    private String parseAppName(ServerWebExchange exchange) {
        RequestPath path = exchange.getRequest().getPath();
        return path.value().split("/")[1];
    }
}
