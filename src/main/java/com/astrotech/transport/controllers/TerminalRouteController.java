package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.request.TerminalRoutePatchRequest;
import com.astrotech.transport.dto.request.TerminalRouteRequest;
import com.astrotech.transport.dto.response.SimpleTerminalRouteResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalRouteResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TerminalRouteService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terminal-route")
@RequiredArgsConstructor
@Tag(name = "TerminalRoute", description = "For adding routes and terminals together, also for updating")
public class TerminalRouteController {
    private final TerminalRouteService terminalRouteService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalRouteResponse>> createTerminalsRoutes(@Valid @RequestBody TerminalRouteRequest request) {
        var response = terminalRouteService.createTerminalRoutes(request);
        return ApiResponseBuilder.success("Terminal Created Successfully", response, apiCacheControl.noStore());
    }
    @PatchMapping("/{terminalRouteId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalRouteResponse>> updateTerminalsRoutes(@PathVariable UUID terminalRouteId, @Valid @RequestBody TerminalRoutePatchRequest request) {
        var response = terminalRouteService.patchTerminalRoute(terminalRouteId,request);
        return ApiResponseBuilder.success("Terminal Created Successfully", response, apiCacheControl.noStore());
    }


    @GetMapping("/all")
    @Ratelimit
    @Hidden
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTerminalRouteResponse>>> getAllTerminalRoutes(@RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
                                                                                                        @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                        @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = terminalRouteService.getAllTerminalRoutes(page, size, sortBy);
        return ApiResponseBuilder.success("Terminals with Routes fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));


    }

    @GetMapping("/{terminalRouteId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalRouteResponse>> getTerminalRoute(@PathVariable UUID terminalRouteId) {
        var response = terminalRouteService.getTerminalRoute(terminalRouteId);
        return ApiResponseBuilder.success("Terminal fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

}
