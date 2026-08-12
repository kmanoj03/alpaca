package com.rvy.scanner.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    public static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");

    @Bean
    public Clock clock() {
        return Clock.system(MARKET_ZONE);
    }
}
