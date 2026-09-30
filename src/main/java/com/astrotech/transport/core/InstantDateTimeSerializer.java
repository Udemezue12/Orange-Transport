package com.astrotech.transport.core;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


import java.util.Locale;



public class InstantDateTimeSerializer extends JsonSerializer<Instant> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy hh:mm:ss a",
                    Locale.ENGLISH
            );

    private final ZoneId zoneId;

    public InstantDateTimeSerializer(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public void serialize(
            Instant value,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {

        gen.writeString(
                FORMATTER
                        .withZone(zoneId)
                        .format(value)
        );
    }
}

