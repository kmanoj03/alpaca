package com.rvy.scanner.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, AlpacaProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));

        return builder
                .requestFactory(() -> factory)
                .defaultHeader("APCA-API-KEY-ID", properties.getApiKey() == null ? "" : properties.getApiKey())
                .defaultHeader("APCA-API-SECRET-KEY", properties.getApiSecret() == null ? "" : properties.getApiSecret())
                .build();
    }
}
