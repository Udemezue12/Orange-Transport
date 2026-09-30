package com.astrotech.transport.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.time.*;
import java.time.format.*;


public class DateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    private final ZoneId zoneId;

    public DateTimeDeserializer(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart().appendPattern(".SSS").optionalEnd()
            .optionalStart().appendPattern(".SS").optionalEnd()
            .optionalStart().appendPattern(".S").optionalEnd()
            .toFormatter();


    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, FORMATTER);
        return localDateTime.atZone(zoneId).toOffsetDateTime();
    }


}

