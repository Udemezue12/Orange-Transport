package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.TerminalRouteMapper;
import com.astrotech.transport.repositories.*;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class TerminalRouteService {
    private final TerminalRouteRepository terminalRouteRepository;
    private final RouteRepository routeRepository;
    private final TerminalRepository terminalRepository;

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "terminal-routes", allEntries = true),
            @CacheEvict(value = "route-lists", allEntries = true)
    })
    public TerminalRouteResponse createTerminalRoutes(TerminalRouteRequest request) {
        var route = findRoute(request.routeId());
        var terminal = findTerminal(request.terminalId());
        validateTerminalForRoute(route, terminal);

        validateTerminalNotAlreadyOnRoute(route.getId(), terminal.getId());

        validateStopOrderNotAlreadyUsed(route.getId(), request.stopOrder());

        var terminalRouteMapper = TerminalRouteMapper.createTerminalRoute(request, route, terminal);
        var savedTerminalRoute = terminalRouteRepository.save(terminalRouteMapper);
        return TerminalRouteMapper.toTerminalRouteResponse(savedTerminalRoute);

    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "terminal-routes", allEntries = true),
            @CacheEvict(value = "route-lists", allEntries = true)
    })
    public TerminalRouteResponse patchTerminalRoute(
            UUID terminalRouteId,
            TerminalRoutePatchRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Terminal route patch request cannot be null"
        );

        var terminalRoute = terminalRouteRepository.findById(
                terminalRouteId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Terminal route does not exist"
                )
        );

        var route = terminalRoute.getRoute();


        if (request.terminalId() != null) {
            updateTerminal(
                    terminalRoute,
                    route,
                    request.terminalId()
            );
        }

        if (request.stopOrder() != null) {
            updateStopOrder(
                    terminalRoute,
                    request.stopOrder()
            );
        }


        var saved = terminalRouteRepository.save(
                terminalRoute
        );

        return TerminalRouteMapper.toTerminalRouteResponse(saved);
    }


    @CustomCacheable(
            value = "terminal-routes",
            key = "'terminal-' + #terminalId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional(readOnly = true)
    public SliceResponse<TerminalRouteResponse> getTerminalRoutes(UUID terminalId, int page, int size, String sortBy) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TerminalRoute.class, true);
        var result = terminalRouteRepository.findAllByTerminalId(terminalId, pageable);

        var content = result
                .getContent()
                .stream()
                .map(TerminalRouteMapper::toTerminalRouteResponse)
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
            value = "route-lists",
            key = "'terminal-' + #routeId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<TerminalRouteResponse> getRoutesTerminal(UUID routeId, int page, int size, String sortBy) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TerminalRoute.class, true);
        var result = terminalRouteRepository.findAllyByRouteId(routeId, pageable);

        var content = result
                .getContent()
                .stream()
                .map(TerminalRouteMapper::toTerminalRouteResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @CustomCacheable(
            value = "terminal-routes",
            key = "'all-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional(readOnly = true)
    public SliceResponse<SimpleTerminalRouteResponse> getAllTerminalRoutes(int page, int size, String sortBy) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TerminalRoute.class, true);
        Slice<TerminalRoute> result = terminalRouteRepository.findAllBy(pageable);

        var content = result
                .getContent()
                .stream()
                .map(TerminalRouteMapper::toSimpleTerminalRouteResponse)
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
    public TerminalRouteResponse getTerminalRoute(UUID terminalRouteId) {

        return terminalRouteRepository.findById(terminalRouteId)
                .map(TerminalRouteMapper::toTerminalRouteResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Does not exist"));
    }

    private void validateTerminalForRoute(Route route, Terminal terminal) {

        if (terminal.equals(route.getOriginTerminal())) {
            throw new ConflictException("The origin terminal is already the starting terminal of this route.");
        }

        if (terminal.equals(route.getDestinationTerminal())) {
            throw new ConflictException("The destination terminal is already the ending terminal of this route.");
        }
    }

    private void validateStopOrderNotAlreadyUsed(UUID id, Integer stopOrder) {
        if (stopOrder != null
                && terminalRouteRepository.existsByRouteIdAndStopOrder(
                id,
                stopOrder
        )) {

            throw new ConflictException(
                    "Stop order " + stopOrder
                            + " is already used on this route."
            );
        }
    }

    private void validateStopOrderNotAlreadyUsed(
            UUID routeId,
            Integer stopOrder,
            UUID terminalRouteId
    ) {
        if (stopOrder == null) {
            return;
        }

        if (stopOrder < 1) {
            throw new ValidationException(
                    "Stop order must be greater than zero."
            );
        }

        boolean alreadyExists =
                terminalRouteRepository
                        .existsByRouteIdAndStopOrderAndIdNot(
                                routeId,
                                stopOrder,
                                terminalRouteId
                        );

        if (alreadyExists) {
            throw new ConflictException(
                    "Stop order " + stopOrder
                            + " is already used on this route."
            );
        }
    }

    private void validateTerminalNotAlreadyOnRoute(
            UUID routeId,
            UUID terminalId
    ) {
        if (terminalRouteRepository.existsByRouteIdAndTerminalId(
                routeId,
                terminalId
        )) {
            throw new ConflictException(
                    "This terminal is already part of the route."
            );
        }
    }

    private void updateTerminal(
            TerminalRoute terminalRoute,
            Route route,
            UUID terminalId
    ) {
        var currentTerminal = terminalRoute.getTerminal();


        if (currentTerminal.getId().equals(terminalId)) {
            return;
        }

        var newTerminal = findTerminal(terminalId);


        validateTerminalForRoute(
                route,
                newTerminal
        );


        boolean alreadyExists =
                terminalRouteRepository
                        .existsByRouteIdAndTerminalIdAndIdNot(
                                route.getId(),
                                newTerminal.getId(),
                                terminalRoute.getId()
                        );

        if (alreadyExists) {
            throw new ConflictException(
                    "This terminal is already part of the route."
            );
        }

        terminalRoute.setTerminal(newTerminal);
    }


    private void updateStopOrder(
            TerminalRoute terminalRoute,
            Integer newStopOrder
    ) {
        var currentStopOrder = terminalRoute.getStopOrder();


        if (Objects.equals(
                currentStopOrder,
                newStopOrder
        )) {
            return;
        }

        validateStopOrderNotAlreadyUsed(
                terminalRoute.getRoute().getId(),
                newStopOrder,
                terminalRoute.getId()
        );

        terminalRoute.setStopOrder(newStopOrder);


        terminalRoute.setStopOrderCreatedAt(
                Instant.now()
        );
    }

    private Route findRoute(UUID routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route does not exist"
                        )
                );
    }


    private Terminal findTerminal(UUID terminalId) {
        return terminalRepository.findById(terminalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Terminal does not exist"
                        )
                );
    }


}
