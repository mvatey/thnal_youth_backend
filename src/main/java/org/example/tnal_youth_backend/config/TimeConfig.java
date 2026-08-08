package org.example.tnal_youth_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Primary;

@Configuration
public class TimeConfig {

    @Bean
    @Primary
    public Clock utcClock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean("systemClock")
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
