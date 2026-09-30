package com.astrotech.transport.controllers;

import com.astrotech.transport.configProperties.AppProperties;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.dto.response.AuthFeaturesResponse;
import com.astrotech.transport.responseBuilder.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Auth Features/Provider", description = "Endpoint for checking if some auth features are enabled or disabled")
@Hidden
@RequiredArgsConstructor
public class AuthFeaturesController {
    private final AppProperties appProperties;
    private final ApiCacheControl apiCacheControl;


    @GetMapping("/config/auth-providers")
    public ResponseEntity<ApiResponse<AuthFeaturesResponse>> checkIfOAuthEnabled() {

        var response = new AuthFeaturesResponse(
                Boolean.TRUE.equals(appProperties.enableCsrf()),
                Boolean.TRUE.equals(appProperties.enableWebAuthn()),
                Boolean.TRUE.equals(appProperties.enableCors()),
                Boolean.TRUE.equals(appProperties.enableOAuth2()),
                Boolean.TRUE.equals(appProperties.requestRegisterCodeForAdmins())
        );


        return ApiResponseBuilder.success("Fetched successfully",
                response,
                apiCacheControl.noStore());
    }


}