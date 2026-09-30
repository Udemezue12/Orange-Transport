package com.astrotech.transport.jobrunr.tasks;


import com.astrotech.transport.entities.Ticket;
import com.astrotech.transport.jobrunr.enqeues.JobsEnqueue;
import com.astrotech.transport.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GenerateTicketTask {
    private final TicketService ticketService;
    private final JobsEnqueue jobsEnqueue;

    @Job(name = "Generate Ticket", retries = 3)
    public void generateTicket(UUID paymentId) {
        var tickets = ticketService.generateTicketWorker(paymentId);
        for (Ticket ticket : tickets ){
            jobsEnqueue.enqueuePdfGeneration(ticket.getId());

        }


    }
}
