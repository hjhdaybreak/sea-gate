package com.sea;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "mapper")
@SpringBootApplication
public class SeaAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeaAdminApplication.class, args);
    }

}
