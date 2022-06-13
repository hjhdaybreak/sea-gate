package com.sea.plugin;

import com.sea.chain.PluginChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface SeaPlugin {

    /**
     * 优先级
     *
     * @return
     */
    Integer order();


    /**
     * 当前 plugin name
     *
     * @return
     */
    String name();


    Mono<Void> execute(ServerWebExchange exchange, PluginChain pluginChain);

}
