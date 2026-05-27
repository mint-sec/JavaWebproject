package com.coldchain.backend;

import com.coldchain.backend.config.DataSourceModeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DataSourceModeProperties.class)
public class ColdChainBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ColdChainBackendApplication.class, args);
    }
}
