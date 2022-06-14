package com.sea.plugin;


import com.sea.config.ServerConfigProperties;

public abstract class AbstractSeaPlugin implements SeaPlugin {

    protected ServerConfigProperties properties;

    public AbstractSeaPlugin(ServerConfigProperties properties) {
        this.properties = properties;
    }

}
