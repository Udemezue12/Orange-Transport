package com.astrotech.transport.jobrunr.enqeues;


import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.jobrunr.tasks.TicketPdfTask;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobsEnqueue {
    private final JobScheduler jobScheduler;
    private final StorageProvider storageProvider;

    @RoleRequired({UserRole.ADMIN, UserRole.DRIVER, UserRole.PASSENGER, UserRole.TERMINAL_SUPERVISOR})
    public Map<String, Object> enqueuePdfGeneration(UUID ticketId) {
        var jobId = jobScheduler.<TicketPdfTask>enqueue(service -> service.generateAndSavePdfJob(ticketId));

        return Map.of(
                "message", "PDF generation queued successfully",
                "jobId", jobId.asUUID()
        );
    }

    public Map<String, String> getJobStatus(UUID jobId) {
        var job = storageProvider.getJobById(jobId);
        if (job == null) {
            return Map.of(
                    "message", "No job found"
            );
        }
        return Map.of(
                "jobId", jobId.toString(),
                "status", job.getState().name()
        );
    }
}
