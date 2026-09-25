package com.finpilot.plaid.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PlaidConfig {

    private final PlaidProperties plaidProperties;

    @Bean
    public PlaidApi plaidApi() {
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", plaidProperties.getClientId());
        apiKeys.put("secret", plaidProperties.getSecret());

        ApiClient apiClient = new ApiClient(apiKeys);
        String env = plaidProperties.getResolvedEnv();

        if ("production".equalsIgnoreCase(env)) {
            log.warn("Plaid Production configured! Sandbox is required for this phase.");
            apiClient.setPlaidAdapter(ApiClient.Production);
        } else {
            apiClient.setPlaidAdapter(ApiClient.Sandbox);
        }

        return apiClient.createService(PlaidApi.class);
    }
}
