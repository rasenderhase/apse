package de.nikem.apse.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApseConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
