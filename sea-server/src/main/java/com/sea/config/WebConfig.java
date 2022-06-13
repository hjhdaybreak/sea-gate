package com.sea.config;


import com.sea.filter.PluginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
public class WebConfig {
    @Bean
    public PluginFilter pluginFilter() {
        return new PluginFilter();
    }
}
