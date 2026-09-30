package com.astrotech.transport.jobrunr.orchestration;


import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Endpoints for everything jobrunr")
public class JobRunrController {
    private final JobRunrManagementService jobManagementService;

    @DeleteMapping("/succeeded")
    @Ratelimit
    public ResponseEntity<Map<String, Object>> purgeSucceededJobs() {
        int deletedCount = jobManagementService.purgeSucceededJobs();

        return ResponseEntity.ok(Map.of(
                "message", "Successfully purged succeeded jobs",
                "deletedCount", deletedCount
        ));
    }

    @DeleteMapping("/deleted")
    @Ratelimit
    public ResponseEntity<Map<String, Object>> purgeDeletedJobs() {
        int deletedCount = jobManagementService.purgeDeletedJobs();

        return ResponseEntity.ok(Map.of(
                "message", "Successfully purged deleted jobs",
                "deletedCount", deletedCount
        ));
    }



}
