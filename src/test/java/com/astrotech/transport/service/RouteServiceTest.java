package com.astrotech.transport.service;

import com.astrotech.transport.dto.request.RouteRequest;
import com.astrotech.transport.dto.response.RouteResponse;
import com.astrotech.transport.dto.response.SimpleTerminalResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.Terminal;
import com.astrotech.transport.mappers.RouteMapper;
import com.astrotech.transport.repositories.RouteRepository;
import com.astrotech.transport.repositories.TerminalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @InjectMocks
    private RouteService routeService;

    private MockedStatic<RouteMapper> routeMapper;


    @BeforeEach
    void setUp() {
        routeMapper = Mockito.mockStatic(RouteMapper.class);
    }

    @AfterEach
    void tearDown() {
        routeMapper.close();
    }

    @Test
    void createRoute() {

//        Arrange
        UUID originTerminalId = UUID.randomUUID();
        UUID destinationTerminalId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();

        RouteRequest request = validRequest(originTerminalId, destinationTerminalId);

        Terminal originTerminal = new Terminal();
        Terminal destinationTerminal = new Terminal();
        Route route = new Route();

        Route savedRoute = new Route();
        savedRoute.setId(routeId);

        RouteResponse expectedResponse = routeResponse(routeId, originTerminalId, destinationTerminalId);

        when(terminalRepository.findById(originTerminalId))
                .thenReturn(Optional.of(originTerminal));
        when(terminalRepository.findById(destinationTerminalId)).thenReturn(Optional.of(destinationTerminal));
        when(routeRepository.existsByOriginTerminalIdAndDestinationTerminalId(originTerminalId, destinationTerminalId)).thenReturn(false);

        routeMapper.when(() -> RouteMapper.createRoute(originTerminal, destinationTerminal)).thenReturn(route);
        when(routeRepository.save(route)).thenReturn(savedRoute);
        routeMapper.when(() -> RouteMapper.toResponse(savedRoute)).thenReturn(expectedResponse);

//        Act
        var actualResponse = routeService.createRoute(request);

//        Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

//        Verify
        verify(terminalRepository).findById(originTerminalId);
        verify(terminalRepository).findById(destinationTerminalId);
        verify(routeRepository).existsByOriginTerminalIdAndDestinationTerminalId(originTerminalId, destinationTerminalId);
        verify(routeRepository).save(route);

        routeMapper.verify(() -> RouteMapper.createRoute(originTerminal, destinationTerminal));
        routeMapper.verify(() ->
                RouteMapper.toResponse(savedRoute)
        );


    }


    private RouteRequest validRequest(UUID originTerminalId, UUID destinationTerminalId) {
        return new RouteRequest(
                originTerminalId,
                destinationTerminalId

        );
    }

    private RouteResponse routeResponse(UUID routeId, UUID originTerminalId, UUID destinationTerminalId) {
        var terminal1 = new SimpleTerminalResponse(
                originTerminalId,
                "Terminal Alpha-7",
                "California",
                "San Francisco",
                "456 Market St, Suite 100"
        );
        SimpleTerminalResponse terminal2 = new SimpleTerminalResponse(
                destinationTerminalId,
                "Echo Logistics Hub",
                "Texas",
                "Austin",
                "1209 Industrial Blvd"
        );
        return new RouteResponse(
                routeId,
                34.09,
                62,
                terminal1,
                terminal2
        );


    }
}