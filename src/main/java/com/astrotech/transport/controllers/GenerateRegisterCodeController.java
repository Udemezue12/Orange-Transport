package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.GeneratedCodeResponse;
import com.astrotech.transport.dto.response.SliceResponse;

import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.GenerateRegisterCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invite-code")
@RequiredArgsConstructor
@Tag(name = "Generate Invite Code", description = "For generating invite code used for all role registrations asides passenger")
public class GenerateRegisterCodeController {
    private final GenerateRegisterCodeService codeService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/generate")
    @Ratelimit
    public ResponseEntity<ApiResponse<GeneratedCodeResponse>> generate(@Valid @RequestBody GenerateRegisterCodeRequest request) {

        var response = codeService.createRegistrationCode(request.role());
        return ApiResponseBuilder.success("Code generated successfully", response, apiCacheControl.noStore());
    }

    @PostMapping("/generate-many")
    @Ratelimit
    public ResponseEntity<ApiResponse<List<GeneratedCodeResponse>>> generateMany(@Valid @RequestBody GenerateRegisterCodesRequest request) {

        var response = codeService.createRegistrationCodes(request.roles(), request.numOfCodes());
        return ApiResponseBuilder.success("Codes generated successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{code}")
    @Ratelimit
    public ResponseEntity<ApiResponse<GeneratedCodeResponse>> getCode(@PathVariable String code) {
        var response = codeService.getCode(code);
        return ApiResponseBuilder.success("Code fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<GeneratedCodeResponse>>> getCodes(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                      @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = codeService.getCodes(page, size);
        return ApiResponseBuilder.success("Codes fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/-past-24hrs")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<GeneratedCodeResponse>>> getCodesPast24hrs(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                               @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = codeService.getCodesGeneratedInPast24Hours(page, size);
        return ApiResponseBuilder.success("Codes fetched successfully", response, apiCacheControl.noStore());
    }
}
