package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.WorkerAssignmentRequest;
import com.astrotech.transport.dto.response.SimpleAssignedWorkerResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalAssignedWorkerResponse;
import com.astrotech.transport.dto.response.TripAssignedWorkerResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.AssignedServiceType;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.AssignedWorkerMapper;
import com.astrotech.transport.repositories.*;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignWorkerService {
    private final AssignedWorkerRepository assignedWorkerRepository;
    private final UserService userService;
    private final TripRepository tripRepository;
    private final TerminalRepository terminalRepository;

    @Transactional
    @CacheEvict(value = "assigned-lists", allEntries = true)
    public void assignWorker(UUID workerId, UUID assignedById, AssignedServiceType serviceType, UUID serviceId) {
        var worker = userService.getAuthorizedUser(workerId);
        var assignedBy = userService.getAuthorizedUser(assignedById);
        var assignedMapper = AssignedWorkerMapper.assign(worker, assignedBy, serviceType, serviceId);
        assignedWorkerRepository.save(assignedMapper);

    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "assigned-lists", allEntries = true),
                    @CacheEvict(value = "unassigned-lists", allEntries = true)

            }
    )
    public void assignWorkers(
            @Valid List<WorkerAssignmentRequest> assignments,
            UUID assignedById,
            UUID serviceId
    ) {
        Objects.requireNonNull(assignments, "AssignmentRequest cannot be null");
        var assignedBy = userService.getAuthorizedUser(assignedById);
        var workerIds = assignments.stream()
                .map(WorkerAssignmentRequest::workerId)
                .distinct()
                .toList();
        var workers = userService.findAll(workerIds, UserStatus.ACTIVE);
        if (workers.size() != workerIds.size()) {
            throw new ResourceNotFoundException("One or more workers were not found");
        }
        var workersById = workers.stream()
                .collect(
                        Collectors.toMap(
                                User::getId,
                                Function.identity()
                        )
                );
        var workerAssignments = assignments.stream()
                .map(request -> {
                    var worker = workersById.get(request.workerId());

                    if (worker == null) {
                        throw new ResourceNotFoundException(
                                "Worker not found: " + request.workerId()
                        );
                    }

                    return AssignedWorkerMapper.assign(
                            worker,
                            assignedBy,
                            request.serviceType(),
                            serviceId
                    );
                })
                .toList();

        assignedWorkerRepository.saveAll(workerAssignments);
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "assigned",
                    allEntries = true),
            @CacheEvict(value = "assigned-lists",
                    allEntries = true),
            @CacheEvict(value = "unassigned-lists", allEntries = true)

    })
    public void updateAssignedWorker(
            UUID serviceId,
            User assignedWorker,
            User currentUser,
            UserRole requiredRole,
            AssignedServiceType serviceType
    ) {
        if (assignedWorker == null) {
            throw new BadRequestException(
                    "A worker must be provided."
            );
        }

        if (assignedWorker.getRole() != requiredRole) {
            throw new BadRequestException(
                    "The assigned worker must be " + requiredRole + "."
            );
        }

        var assignedService = findByServiceId(serviceId);

        if (assignedService == null) {
            throw new ResourceNotFoundException(
                    "No assigned service exists for this service."
            );
        }

        assignedService.setWorker(assignedWorker);
        assignedService.setAssignedBy(currentUser);
        assignedService.setAssignedServiceType(serviceType);
        assignedService.setAssignedServiceId(serviceId);
    }

    @Caching(evict = {
            @CacheEvict(value = "assigned", allEntries = true),
            @CacheEvict(value = "assigned-lists", allEntries = true)
    })
    @Transactional
    public void updateAssignStatus(UUID serviceId, AssignStatus assignStatus) {
        assignedWorkerRepository.updateAssignStatusByServiceId(serviceId, assignStatus);
    }


    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN,
            UserRole.TERMINAL_SUPERVISOR
    })
    @CustomCacheable(
            value = "assigned-lists",
            key = "'all-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleAssignedWorkerResponse> getAllAssignedWorkers(int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, AssignedWorker.class, true);
        var result = assignedWorkerRepository.findAllBy(pageable);
        var content = result.getContent()
                .stream()
                .map(AssignedWorkerMapper::simpleResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "assigned",
            key = "'tripService-' + #serviceId + '-serviceType' + #serviceType",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TripAssignedWorkerResponse getTripAssignedWorker(UUID serviceId, AssignedServiceType serviceType) {
        if (serviceType != AssignedServiceType.TRIP_VEHICLE_LOADER
                && serviceType != AssignedServiceType.TRIP_VEHICLE_DRIVER) {
            throw new BadRequestException("Unsupported service type: " + serviceType);
        }

        var assigned = findByServiceIdAndServiceType(serviceId, serviceType);
        var trip = tripRepository.findById(assigned.getAssignedServiceId()).orElseThrow(() -> new ResourceNotFoundException("Assigned Trip not Found"));


        return AssignedWorkerMapper.tripAssignedWorkerResponse(assigned, trip);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "assigned",
            key = "'terminalService-' + #serviceId + '-serviceType' + #serviceType",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TerminalAssignedWorkerResponse getTerminalAssignedWorker(UUID serviceId, AssignedServiceType serviceType) {
        if (serviceType != AssignedServiceType.TERMINAL) {
            throw new BadRequestException("Unsupported service type: " + serviceType);
        }

        var assigned = findByServiceIdAndServiceType(serviceId, serviceType);
        var terminal = terminalRepository.findById(assigned.getAssignedServiceId()).orElseThrow(() -> new ResourceNotFoundException("Assigned Terminal not Found"));


        return AssignedWorkerMapper.terminalAssignedWorkerResponse(assigned, terminal);
    }

    public AssignedWorker findByServiceId(UUID serviceId) {
        return assignedWorkerRepository.findByAssignedServiceId(serviceId).orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    public AssignedWorker findByServiceIdAndServiceType(UUID serviceId, AssignedServiceType serviceType) {
        return assignedWorkerRepository.findByAssignedServiceIdAndAssignedServiceType(serviceId, serviceType).orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }


}
