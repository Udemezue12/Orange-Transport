package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.request.TerminalRequest;
import com.astrotech.transport.dto.request.TerminalUpdateRequest;
import com.astrotech.transport.dto.response.SimpleTerminalResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalResponse;
import com.astrotech.transport.dto.response.TerminalRouteResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TerminalRouteService;
import com.astrotech.transport.service.TerminalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terminal")
@RequiredArgsConstructor
@Tag(name = "Terminal", description = "For creating, updating terminal..Also for getting a terminal or terminals")
public class TerminalController {
    private final TerminalService terminalService;
    private final TerminalRouteService terminalRouteService;
    private final ApiCacheControl apiCacheControl;


    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalResponse>> createTerminal(@Valid @RequestBody TerminalRequest request) {

        var response = terminalService.createTerminal(request);
        return ApiResponseBuilder.success("Terminal Created Successfully", response, apiCacheControl.noStore());
    }

    @PostMapping("/{terminalId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalResponse>> updateTerminal(@Valid @RequestBody TerminalUpdateRequest request, @PathVariable UUID terminalId) {
        var response = terminalService.updateTerminal(request, terminalId);
        return ApiResponseBuilder.success("Terminal Updated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTerminalResponse>>> getAllTerminals(@RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
                                                                                              @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                              @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = terminalService.getAllTerminals(page, size, sortBy, true);
        return ApiResponseBuilder.success("Terminals fetched Successfully", response, apiCacheControl.publicMaxAgeMinutes(15));


    }

    @GetMapping("/list")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTerminalResponse>>> getListTerminals(@RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
                                                                                               @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                               @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = terminalService.getAllTerminals(page, size, sortBy, false);
        return ApiResponseBuilder.success("Terminals fetched Successfully", response, apiCacheControl.publicMaxAgeMinutes(15));


    }

    @GetMapping("/{terminalId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<TerminalRouteResponse>>> getTerminalWithRoutes(@PathVariable UUID terminalId, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
                                                                                                   @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                   @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = terminalRouteService.getTerminalRoutes(terminalId, page, size, sortBy);
        return ApiResponseBuilder.success("Terminal with Routes fetched Successfully", response, apiCacheControl.publicMaxAgeMinutes(15));


    }

    @GetMapping("/{terminalId}/get-terminal")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalResponse>> getTerminal(@PathVariable UUID terminalId) {
        var response = terminalService.getTerminal(terminalId);
        return ApiResponseBuilder.success("Terminal fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));


    }

}
