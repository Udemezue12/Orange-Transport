package com.astrotech.transport.fintech.data;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.astrotech.transport.serializers.StatusDeserializer;

import lombok.Data;

@Data
public class PaymentApiResponse<T> {
    @JsonDeserialize(using = StatusDeserializer.class)
    private boolean status;
    private String message;
    private T data;
}

