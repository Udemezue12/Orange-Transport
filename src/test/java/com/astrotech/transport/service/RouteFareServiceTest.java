package com.astrotech.transport.service;

import com.astrotech.transport.dto.request.RouteFareRequest;
import com.astrotech.transport.dto.response.RouteFareResponse;
import com.astrotech.transport.dto.response.RouteResponse;
import com.astrotech.transport.dto.response.SimpleRouteFareResponse;
import com.astrotech.transport.dto.response.SimpleTerminalResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.RouteFare;
import com.astrotech.transport.enums.VehicleClass;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.RouteFareMapper;
import com.astrotech.transport.repositories.RouteFareRepository;
import com.astrotech.transport.repositories.RouteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RouteFareServiceTest {
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteFareRepository routeFareRepository;
    @InjectMocks
    private RouteFareService routeFareService;

    private MockedStatic<RouteFareMapper> routeFareMapper;

    @BeforeEach
    void setUp() {

        routeFareMapper = Mockito.mockStatic(RouteFareMapper.class);
    }

    @AfterEach
    void tearDown() {

        routeFareMapper.close();
    }

    @Test
    void shouldCreateRouteFareSuccessfully() {
        UUID routeId = UUID.randomUUID();
        UUID routeFareId = UUID.randomUUID();

        RouteFareRequest request = validRequest();

        Route route = new Route();
        RouteFare routeFare = new RouteFare();

        RouteFare savedRouteFare = new RouteFare();
        savedRouteFare.setId(routeFareId);


        RouteFareResponse expectedResponse = routeFareResponse(routeId, routeFareId);

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));

        when(routeFareRepository.existsByRouteIdAndVehicleClass(routeId, request.vehicleClass())
        ).thenReturn(false);

        routeFareMapper.when(() ->
                RouteFareMapper.createRouteFare(request, route)
        ).thenReturn(routeFare);

        when(routeFareRepository.save(routeFare)).thenReturn(savedRouteFare);

        routeFareMapper.when(() ->
                RouteFareMapper.routeFareResponse(savedRouteFare)
        ).thenReturn(expectedResponse);

        RouteFareResponse actualResponse =
                routeFareService.createRouteFare(request, routeId);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(routeRepository).findById(routeId);


        verify(routeFareRepository)
                .existsByRouteIdAndVehicleClass(
                        routeId,
                        request.vehicleClass()
                );

        verify(routeFareRepository).save(routeFare);

        routeFareMapper.verify(() ->
                RouteFareMapper.createRouteFare(request, route)
        );

        routeFareMapper.verify(() ->
                RouteFareMapper.routeFareResponse(savedRouteFare)
        );

    }

    @Test
    void shouldThrowWhenRouteDoesNotExist() {
        UUID routeId = UUID.randomUUID();

        RouteFareRequest request = validRequest();

        when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, ()-> routeFareService.createRouteFare(request, routeId));

        verify(routeFareRepository, never()).existsByRouteIdAndVehicleClass(routeId, request.vehicleClass());

        verify(routeFareRepository, never()).save(
                ArgumentMatchers.any(RouteFare.class)
        );
    }


    private RouteFareRequest validRequest() {
        return new RouteFareRequest(
                BigDecimal.valueOf(5000),
                Instant.parse("2026-10-01T00:00:00Z"),
                Instant.parse("2026-11-01T00:00:00Z"),
                VehicleClass.STANDARD
        );
    }


    private RouteFareResponse routeFareResponse(UUID routeId, UUID routeFareId) {
        var terminal1 = new SimpleTerminalResponse(
                UUID.fromString("6f8b92d4-1a5c-4e72-8f1a-3b9c5d7e1234"),
                "Terminal Alpha-7",
                "California",
                "San Francisco",
                "456 Market St, Suite 100"
        );
        SimpleTerminalResponse terminal2 = new SimpleTerminalResponse(
                UUID.fromString("b0c39f1e-82d4-4a6c-92b1-5e7f3d2c1b0a"),
                "Echo Logistics Hub",
                "Texas",
                "Austin",
                "1209 Industrial Blvd"
        );
        var routeResponse = new RouteResponse(
                routeId,
                34.09,
                62,
                terminal1,
                terminal2
        );

        var simpleRouteFareResponse =
                new SimpleRouteFareResponse(
                        routeFareId,
                        BigDecimal.valueOf(5000),
                        Instant.parse("2026-10-01T00:00:00Z"),
                        Instant.parse("2026-11-01T00:00:00Z")
                );
        return new RouteFareResponse(
                routeResponse,
                simpleRouteFareResponse

        );
    }

}