package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.SimpleAssignedWorkerResponse;
import com.astrotech.transport.dto.response.TerminalAssignedWorkerResponse;
import com.astrotech.transport.dto.response.TripAssignedWorkerResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.AssignedServiceType;

import java.time.Instant;
import java.util.UUID;

public class AssignedWorkerMapper {
    public static AssignedWorker assign(User worker, User assignedBy, AssignedServiceType serviceType,UUID assignedServiceId) {
        return AssignedWorker.builder()
                .assignedAt(Instant.now())
                .assignStatus(AssignStatus.ACTIVE)
                .worker(worker)
                .assignedBy(assignedBy)
                .assignedServiceId(assignedServiceId)
                .assignedServiceType(serviceType)
                .build();
    }
    public static SimpleAssignedWorkerResponse simpleResponse(AssignedWorker assignedWorker){
        return new SimpleAssignedWorkerResponse(
                assignedWorker.getId(),
                assignedWorker.getAssignedServiceType(),
                assignedWorker.getAssignedServiceId(),
                assignedWorker.getAssignStatus(),
                assignedWorker.getAssignedAt()

        );
    }
    public static TripAssignedWorkerResponse tripAssignedWorkerResponse(AssignedWorker assignedWorker, Trip trip ){
        var workerResponse = UserMapper.response(assignedWorker.getWorker());
        var assignedBy = UserMapper.response(assignedWorker.getAssignedBy());
        var assignedResponse = simpleResponse(assignedWorker);
        var tripResponse = TripMapper.mapToSimpleResponse(trip);
        return new TripAssignedWorkerResponse(
                assignedResponse,
                assignedBy,
                workerResponse,
                tripResponse
        );
    }
    public static TerminalAssignedWorkerResponse terminalAssignedWorkerResponse(AssignedWorker assignedWorker, Terminal terminal ){
        var workerResponse = UserMapper.response(assignedWorker.getWorker());
        var assignedBy = UserMapper.response(assignedWorker.getAssignedBy());
        var assignedResponse = simpleResponse(assignedWorker);
        var terminalResponse = TerminalMapper.simpleResponse(terminal);
        return new TerminalAssignedWorkerResponse(
                assignedResponse,
                assignedBy,
                workerResponse,
                terminalResponse
        );
    }
}
