package com.astrotech.transport.controllers;

import com.astrotech.transport.configProperties.AppProperties;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.dto.response.CsrfResponse;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/csrf_token")
@Tag(name = "Csrf Token", description = "Endpoints for csrfToken generation")
@RequiredArgsConstructor
public class CsrfController {



    @GetMapping
    public ResponseEntity<CsrfResponse> csrf(
            CsrfToken csrfToken) {

        return ResponseEntity.ok(
                new CsrfResponse(
                        csrfToken.getToken(),
                        csrfToken.getHeaderName(),
                        csrfToken.getParameterName()
                )
        );
    }





}
