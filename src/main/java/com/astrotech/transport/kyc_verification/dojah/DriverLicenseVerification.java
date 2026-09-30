package com.astrotech.transport.kyc_verification.dojah;


import com.astrotech.transport.dto.response.DriverLicenseResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DriverLicenseVerification {
    private final RestClient restClient;
    private final WebClient webClient;

    public DriverLicenseVerification(@Qualifier("dojahRestClient") RestClient restClient, @Qualifier("dojahWebClient") WebClient webClient) {
        this.restClient = restClient;
        this.webClient = webClient;
    }
    public DriverLicenseResponse restClientVerifyLicence(String licenceNumber) {
        return restClient.get()
                .uri(uri -> uri
                        .path("/api/v1/kyc/dl")
                        .queryParam("license_number", licenceNumber)
                        .build())
                .retrieve()
                .body(DriverLicenseResponse.class);
    }
    public Mono<DriverLicenseResponse> webClientVerifyLicence(String licenceNumber) {
        return webClient.get()
                .uri(uri -> uri
                        .path("/api/v1/kyc/dl")
                        .queryParam("license_number", licenceNumber)
                        .build())
                .retrieve()
                .bodyToMono(DriverLicenseResponse.class);
    }

}
