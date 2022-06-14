package com.sea.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class SeaThreadFactory {

    private String name;

    private Boolean daemon;

    public SeaThreadFactory(String name, Boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    public ThreadFactory create() {
        return new ThreadFactoryBuilder().setNameFormat(this.name + "-%d").setDaemon(this.daemon).build();
    }
}
