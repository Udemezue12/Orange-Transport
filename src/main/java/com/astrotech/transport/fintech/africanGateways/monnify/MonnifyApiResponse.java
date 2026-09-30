package com.astrotech.transport.fintech.africanGateways.monnify;


public record MonnifyApiResponse<T>(
        boolean requestSuccessful,
        String responseMessage,
        String responseCode,
        T responseBody
) {}
