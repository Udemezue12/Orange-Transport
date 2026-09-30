package com.astrotech.transport.service;

import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.RouteRequest;
import com.astrotech.transport.dto.request.RouteUpdateRequest;
import com.astrotech.transport.dto.response.RouteResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.Terminal;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.RouteMapper;
import com.astrotech.transport.repositories.RouteRepository;
import com.astrotech.transport.repositories.TerminalRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RouteService {
    private final RouteRepository routeRepository;
    private final TerminalRepository terminalRepository;

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @CacheEvict(value = "route-lists", allEntries = true)
    public RouteResponse createRoute(RouteRequest request) {
        var originTerminalId = request.originTerminalId();
        var destinationTerminalId = request.destinationTerminalId();

        if (originTerminalId.equals(destinationTerminalId)) {
            throw new BadRequestException("Origin and Destination terminals cannot be the same.");
        }

        var terminalIds = List.of(originTerminalId, destinationTerminalId);

        var terminalMap = terminalRepository.findAllById(terminalIds)
                .stream()
                .collect(Collectors.toMap
                        (Terminal::getId,
                                Function.identity())
                );

        var originTerminal = Optional.ofNullable(terminalMap.get(originTerminalId))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Origin terminal not found: " + originTerminalId
                        )
                );

        var destinationTerminal = Optional.ofNullable(terminalMap.get(destinationTerminalId))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Destination terminal not found: " + destinationTerminalId
                        )
                );

        if (routeRepository.existsByOriginTerminalIdAndDestinationTerminalId(originTerminalId, destinationTerminalId)) {
            throw new ConflictException("A route between these terminals already exists.");
        }

        var route = RouteMapper.createRoute(originTerminal, destinationTerminal);
        var savedRoute = routeRepository.save(route);

        return RouteMapper.toResponse(savedRoute);
    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "routes", key = "#routeId"),
            @CacheEvict(value = "route-lists", allEntries = true),
            @CacheEvict(value = "route-with-fares", allEntries = true)
    })
    public RouteResponse updateRoute(RouteUpdateRequest request, UUID routeId) {
        var route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route does not exist: " + routeId));

        var newOriginId = request.originTerminalId() != null ? request.originTerminalId() : route.getOriginTerminal().getId();
        var newDestinationId = request.destinationTerminalId() != null ? request.destinationTerminalId() : route.getDestinationTerminal().getId();

        if (newOriginId.equals(newDestinationId)) {
            throw new BadRequestException("Origin and Destination terminals cannot be the same.");
        }


        var isOriginChanged = request.originTerminalId() != null && !request.originTerminalId().equals(route.getOriginTerminal().getId());

        var isDestinationChanged = request.destinationTerminalId() != null && !request.destinationTerminalId().equals(route.getDestinationTerminal().getId());

        if (isOriginChanged || isDestinationChanged) {
            if (routeRepository.existsByOriginTerminalIdAndDestinationTerminalId(newOriginId, newDestinationId)) {
                throw new ConflictException("A route between these terminals already exists.");
            }
        }

        if (isOriginChanged) {
            Terminal newOrigin = terminalRepository.findById(newOriginId)
                    .orElseThrow(() -> new ResourceNotFoundException("Origin terminal not found: " + newOriginId));
            route.setOriginTerminal(newOrigin);
        }

        if (isDestinationChanged) {
            Terminal newDestination = terminalRepository.findById(newDestinationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Destination terminal not found: " + newDestinationId));
            route.setDestinationTerminal(newDestination);
        }


        var savedRoute = routeRepository.save(route);
        return RouteMapper.toResponse(savedRoute);
    }


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "route-lists",
            key = "'all-p' + #page + '-s' + #size",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<RouteResponse> getAllRoutes(int page, int size, Boolean usePageable) {

        if (!Boolean.TRUE.equals(usePageable)) {

            var content = routeRepository.findAll()
                    .stream()
                    .map(RouteMapper::toResponse)
                    .toList();

            return getSliceResponse(content, 0,
                    content.size(),
                    false,
                    false);
        }

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "name", true, Route.class, true);
        var result = routeRepository.findAllBy(pageable);

        var content = result
                .getContent()
                .stream()
                .map(RouteMapper::toResponse)
                .toList();
        return getSliceResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private static @NonNull SliceResponse<RouteResponse> getSliceResponse(List<RouteResponse> content, int page, int size, boolean hasNext, boolean hasPrevious) {
        return new SliceResponse<>(
                content,
                page,
                size,
                hasNext,
                hasPrevious
        );
    }

}

