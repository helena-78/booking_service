package com.sportlink.moderation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${clients.account-management.base-url}")
    private String accountManagementBaseUrl;

    @Bean
    public WebClient accountManagementWebClient(WebClient.Builder builder) {
        return builder.baseUrl(accountManagementBaseUrl).build();
    }
}
