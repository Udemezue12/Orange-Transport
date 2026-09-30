package com.astrotech.transport.serializers;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StatusDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(
            JsonParser parser,
            DeserializationContext ctxt)
            throws IOException {

        JsonToken token = parser.currentToken();

        if (token == JsonToken.VALUE_TRUE) {
            return true;
        }

        if (token == JsonToken.VALUE_FALSE) {
            return false;
        }

        if (token == JsonToken.VALUE_STRING) {
            String value = parser.getValueAsString();

            return "success".equalsIgnoreCase(value)
                    || "successful".equalsIgnoreCase(value);
        }

        return false;
    }
}