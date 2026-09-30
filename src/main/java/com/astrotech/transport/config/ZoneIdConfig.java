package com.astrotech.transport.config;

import com.astrotech.transport.configProperties.ZoneIdProperties;
import lombok.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
public class ZoneIdConfig {
    private  final ZoneIdProperties zoneIdProperties;
    @Bean
    public ZoneId getZone() {
        return  ZoneId.of(zoneIdProperties.zone());
    }

}

