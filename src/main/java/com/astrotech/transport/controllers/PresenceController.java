package com.astrotech.transport.controllers;


import com.astrotech.transport.dto.response.PresenceResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.service.PresenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/presence")
@Tag(name = "Presence", description = "Updating User Presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;


    @GetMapping("/{userId}")
    @Ratelimit
    public PresenceResponse getPresence(
            @PathVariable String userId) {
        return presenceService.getPresence(userId);
    }

    @GetMapping("/{userId}/online-check")
    @Ratelimit
    public boolean checkOnline(
            @PathVariable String userId) {
        return presenceService.isOnline(userId);
    }

    @GetMapping("/{userId}/connection-count-get")
    @Ratelimit
    public long getConnectionCount(
            @PathVariable String userId) {
        return presenceService.getConnectionCount(userId);
    }

    @GetMapping("/{userId}/heartbeat")
    @Ratelimit
    public ResponseEntity<Void> heartBeat(@PathVariable String userId) {
        presenceService.heartbeat(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/connect")
    @Ratelimit
    public ResponseEntity<Void> connect(@PathVariable String userId) {
        presenceService.connect(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/last-heartbeat")
    @Ratelimit
    public Instant getLastHeartBeat(@PathVariable String userId) {
        return presenceService.getLastHeartbeat(userId);


    }


    @PostMapping("/batch")
    @Ratelimit

    public List<PresenceResponse> getBatchPresence(
            @RequestBody Map<String, List<String>> body) {
        return presenceService.getBatchPresence(body);
    }


    @PatchMapping("/status")
    @Ratelimit
    public Map<String, Object> updateStatus(
            @RequestBody Map<String, String> body) {
        return presenceService.updateStatus(body);

    }
}
