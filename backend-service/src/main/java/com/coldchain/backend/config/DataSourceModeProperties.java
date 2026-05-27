package com.coldchain.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource")
public class DataSourceModeProperties {
    private String mode = "mock";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean useMysql() {
        return "mysql".equalsIgnoreCase(mode);
    }
}
