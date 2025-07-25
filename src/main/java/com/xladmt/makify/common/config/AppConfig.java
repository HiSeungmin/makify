package com.xladmt.makify.common.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    String apiKey = "8570613034755888";
    String secretKey = "k1bBVPA4sPMWQYvm8yEWCFv3H3k0uVzDfvJXrIvEF8n4rO2loJwQAUZu8AzCAc8WTbv2FIqHAqkuFC67";

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(apiKey, secretKey);
    }
}
