package com.astrotech.transport.config;

import com.astrotech.transport.core.InstantDateTimeSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Configuration
@RequiredArgsConstructor
public class JacksonConfig {

    private final ZoneId zoneId;

    private static final DateTimeFormatter HUMAN_READABLE =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {

        return builder -> builder.serializers(
                new JsonSerializer<Instant>() {

                    @Override
                    public void serialize(
                            Instant instant,
                            JsonGenerator gen,
                            SerializerProvider serializers
                    ) throws IOException {

                        String formatted =
                                HUMAN_READABLE
                                        .withZone(zoneId)
                                        .format(instant);

                        gen.writeString(formatted);
                    }
                }
        );
    }
    @Bean
    public Module instantDateTimeModule() {
        SimpleModule module = new SimpleModule();

        module.addSerializer(
                Instant.class,
                new InstantDateTimeSerializer(zoneId)
        );

        return module;
    }
}


