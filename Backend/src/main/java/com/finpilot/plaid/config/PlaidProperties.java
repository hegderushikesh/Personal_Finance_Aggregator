package com.finpilot.plaid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "plaid")
@Data
public class PlaidProperties {
    private String clientId;
    private String secret;
    private String env = "sandbox";
    private String environment = "sandbox";

    public String getResolvedEnv() {
        if (env != null && !env.isBlank()) return env;
        if (environment != null && !environment.isBlank()) return environment;
        return "sandbox";
    }
}
