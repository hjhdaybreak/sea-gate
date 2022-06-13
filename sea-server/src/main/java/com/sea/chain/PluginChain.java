package com.sea.chain;

import com.sea.plugin.SeaPlugin;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PluginChain implements SeaPlugin {

    private int pos;

    private List<SeaPlugin> plugins;

    public void addPlugin(SeaPlugin seaPlugin) {
        if (plugins == null) {
            plugins = new ArrayList<>();
        }
        plugins.add(seaPlugin);
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

}
