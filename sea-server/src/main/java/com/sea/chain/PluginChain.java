package com.sea.chain;

import com.sea.cache.PluginCache;
import com.sea.config.ServerConfigProperties;
import com.sea.plugin.AbstractSeaPlugin;
import com.sea.plugin.SeaPlugin;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PluginChain extends AbstractSeaPlugin {

    private int pos;

    private List<SeaPlugin> plugins;

    private final String appName;

    public PluginChain(ServerConfigProperties properties, String appName) {
        super(properties);
        this.appName = appName;
    }

    /**
     * add enabled plugin to chain
     *
     * @param seaPlugin
     */
    public void addPlugin(SeaPlugin seaPlugin) {
        if (plugins == null) {
            plugins = new ArrayList<>();
        }
        if (!PluginCache.isEnable(appName, seaPlugin.name())) {
            return;
        }
        plugins.add(seaPlugin);
        // order by the plugin's order
        plugins.sort(Comparator.comparing(SeaPlugin::order));
    }


    @Override
    public Integer order() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain pluginChain) {
        if (pos == plugins.size()) {
            return exchange.getResponse().setComplete();
        }
        return plugins.get(pos++).execute(exchange, pluginChain);
    }

    public String getAppName() {
        return appName;
    }
}
