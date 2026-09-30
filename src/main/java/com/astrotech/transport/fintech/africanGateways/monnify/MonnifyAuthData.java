package com.astrotech.transport.fintech.africanGateways.monnify;



import com.fasterxml.jackson.annotation.JsonProperty;



public record MonnifyAuthData(
        @JsonProperty("accessToken") String accessToken,
        @JsonProperty("expiresIn") int expiresIn
) {}


