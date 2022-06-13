package com.sea;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {AutoRegisterListener.class})
@EnableConfigurationProperties(value = {ClientConfigProperties.class})
public class ClientAutoConfigure {

}
