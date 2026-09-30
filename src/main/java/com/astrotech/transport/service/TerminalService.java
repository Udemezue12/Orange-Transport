package com.astrotech.transport.service;


import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.TerminalRequest;
import com.astrotech.transport.dto.request.TerminalUpdateRequest;
import com.astrotech.transport.dto.response.SimpleTerminalResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalResponse;
import com.astrotech.transport.entities.Terminal;
import com.astrotech.transport.enums.AssignedServiceType;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.enums.VerificationStatus;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.TerminalMapper;
import com.astrotech.transport.repositories.ProfileRepository;
import com.astrotech.transport.repositories.TerminalRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class TerminalService {
    private final TerminalRepository terminalRepository;
    private final ProfileRepository profileRepository;

    private final AssignWorkerService assignWorkerService;
    private final GetCurrentUser getCurrentUser;


    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @CacheEvict(value = "terminal-lists", allEntries = true)
    public TerminalResponse createTerminal(TerminalRequest request) {
        var terminalName = TrimWhiteSpace.trimWhiteSpace(request.terminalName());
        var supervisorId = UUID.fromString(request.supervisorId());
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();

        var existingVehicles = terminalRepository.findExistingFields(terminalName, request.address(), supervisorId);

        if (!existingVehicles.isEmpty()) {
            if (existingVehicles.stream().anyMatch(u -> terminalName.equalsIgnoreCase(u.getName()))) {
                throw new BadRequestException("Terminal already exists");
            }
            if (existingVehicles.stream().anyMatch(u -> request.address().equalsIgnoreCase(u.getAddress()))) {
                throw new BadRequestException("Terminal with this address  already exists");
            }
            if (existingVehicles.stream().anyMatch(u -> supervisorId.equals(u.getSupervisorId()))) {
                throw new BadRequestException("Supervisor has already been assigned to a terminal");
            }

        }
        var profile = profileRepository.findByUserIdAndUserStatusAndRole(supervisorId, UserStatus.ACTIVE, UserRole.TERMINAL_SUPERVISOR).orElseThrow(() -> new ResourceNotFoundException("User profile not found for ID: " + supervisorId + ". Please notify the user to  create a profile"));
        if (profile.getIdentityDocument().getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("Profile is yet to be approved by the management");
        }
        var user = profile.getUser();
        var terminal = TerminalMapper.createTerminal(request, user);
        var savedTerminal = terminalRepository.save(terminal);
        assignWorkerService.assignWorker(savedTerminal.getTerminalSupervisor().getId(), currentUserId, AssignedServiceType.TERMINAL, savedTerminal.getId());


        return TerminalMapper.toResponse(savedTerminal);


    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "terminals", key = "#terminalId"),
            @CacheEvict(value = "terminal-lists", allEntries = true)
    })
    public TerminalResponse updateTerminal(
            TerminalUpdateRequest request,
            UUID terminalId) {

        var currentUser = getCurrentUser.getCurrentUser();

        var terminalName = TrimWhiteSpace.trimWhiteSpace(request.terminalName());
        var city = TrimWhiteSpace.trimWhiteSpace(request.city());
        var state = TrimWhiteSpace.trimWhiteSpace(request.state());
        var address = TrimWhiteSpace.trimWhiteSpace(request.address());

        var terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Terminal does not exist"));

        if (terminalName != null) {
            if (!terminalName.equalsIgnoreCase(terminal.getName())
                    && terminalRepository.existsByName(terminalName)) {

                throw new ConflictException(
                        "Terminal with the name already exists."
                );
            }

            terminal.setName(terminalName);
        }

        if (city != null) {
            if (!city.equalsIgnoreCase(terminal.getCity())
                    && terminalRepository.existsByCity(city)) {

                throw new ConflictException("City already exists.");
            }

            terminal.setCity(city);
        }

        if (state != null) {
            if (!state.equalsIgnoreCase(terminal.getState())
                    && terminalRepository.existsByState(state)) {

                throw new ConflictException("State already exists.");
            }

            terminal.setState(state);
        }

        if (address != null) {
            if (!address.equalsIgnoreCase(terminal.getAddress())
                    && terminalRepository.existsByAddress(address)) {

                throw new ConflictException(
                        "Terminal Address already exists."
                );
            }

            terminal.setAddress(address);
        }


        if (request.supervisorId() != null) {

            var profile = profileRepository.findByUserIdAndUserStatusAndRole(request.supervisorId(), UserStatus.ACTIVE, UserRole.TERMINAL_SUPERVISOR)
                    .orElseThrow(() -> new ResourceNotFoundException("User profile not found for ID: "
                            + request.supervisorId() + ". Please notify the user to  create a profile"));
            if (profile.getIdentityDocument().getVerificationStatus()
                    != VerificationStatus.APPROVED) {
                throw new BadRequestException("Profile is yet to " +
                        "be approved by the management");
            }
            var user = profile.getUser();


            if (terminalRepository.existsByTerminalSupervisorIdAndIdNot(
                    user.getId(),
                    terminal.getId())) {

                throw new ConflictException(
                        "This supervisor is already assigned to another terminal."
                );
            }

            terminal.setTerminalSupervisor(user);


            assignWorkerService.updateAssignedWorker(terminal.getId(), user, currentUser, UserRole.TERMINAL_SUPERVISOR, AssignedServiceType.TERMINAL);
        }


        var savedTerminal = terminalRepository.save(terminal);

        return TerminalMapper.toResponse(savedTerminal);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "terminal-lists",
            key = "'p-' + #page + '-s-' + #size + '-sort-' + #sortBy",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTerminalResponse> getAllTerminals(
            int page,
            int size,
            String sortBy,
            Boolean usePageable) {

        if (!Boolean.TRUE.equals(usePageable)) {

            var content = terminalRepository.findAll()
                    .stream()
                    .map(TerminalMapper::simpleResponse)
                    .toList();

            return getSliceResponse(content, 0,
                    content.size(),
                    false,
                    false);
        }

        var pageable = GetPageRequest.getPageableWithSorting(
                page,
                size,
                sortBy,
                true,
                Terminal.class,
                true
        );

        var result = terminalRepository.findAllBy(pageable);

        var content = result.getContent()
                .stream()
                .map(TerminalMapper::simpleResponse)
                .toList();

        return getSliceResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }


    @CustomCacheable(
            value = "terminals",
            key = "#terminalId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional(readOnly = true)
    public TerminalResponse getTerminal(UUID terminalId) {
        return terminalRepository.findById(terminalId)
                .map(TerminalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal Does not exist"));
    }

    private static @NonNull SliceResponse<SimpleTerminalResponse> getSliceResponse(List<SimpleTerminalResponse> content, int page, int size, boolean hasNext, boolean hasPrevious) {
        return new SliceResponse<>(
                content,
                page,
                size,
                hasNext,
                hasPrevious
        );
    }


}
