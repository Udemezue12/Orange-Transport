package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.request.TripRequest;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.TripStatus;

import java.time.temporal.ChronoUnit;

public class TripMapper {
    public static Trip createTrip(TripRequest request,String tripCode, Vehicle vehicle, Route route, User createdBy, DriverProfile driver, Profile vehicleLoader) {
        var bookingCutoff = request.scheduledDepartureTime().minus(30, ChronoUnit.MINUTES);
        return Trip.builder()
                .scheduledDepartureTime(request.scheduledDepartureTime())
                .scheduledArrivalTime(request.scheduledArrivalTime())
                .vehicleLoaderProfile(vehicleLoader)
                .status(TripStatus.SCHEDULED)
                .tripCode(tripCode)
                .boardingTime(request.boardingTime())
                .bookingCutoff(bookingCutoff)
                .vehicle(vehicle)
                .route(route)
                .createdBy(createdBy)
                .driver(driver)
                .build();
    }

    public static SimpleTripResponse mapToSimpleResponse(Trip trip) {
        return new SimpleTripResponse(
                trip.getId(),
                trip.getActualDepartureTime(),
                trip.getActualArrivalTime(),
                trip.getStatus(),
                trip.getTripCode(),
                trip.getBoardingTime(),
                trip.getBookingCutoff(),
                trip.getDelayReason()
        );
    }

    public static TripResponse mapToTripResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());
        var userResponse = UserMapper.response(trip.getCreatedBy());
        var tripResponse = mapToSimpleResponse(trip);
        var vehicleResponse = VehicleMapper.toResponse(trip.getVehicle());
        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new TripResponse(
                tripResponse,
                vehicleResponse,
                routeResponse,
                driverResponse,
                userResponse
        );
    }
    public static TripCodeResponse mapToTripCodeResponse(Trip trip) {
        DriverProfileResponse driverResponse = DriverProfileMapper.response(trip.getDriver());
        SimpleTripResponse tripResponse = mapToSimpleResponse(trip);
        VehicleResponse vehicleResponse = VehicleMapper.toResponse(trip.getVehicle());
        RouteResponse routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new TripCodeResponse(
                tripResponse,
                vehicleResponse,
                routeResponse,
                driverResponse
        );
    }
    public static TripWithoutUserResponse mapToResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());

        var tripResponse = mapToSimpleResponse(trip);

        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new TripWithoutUserResponse(
                tripResponse,
                routeResponse,
                driverResponse
        );
    }

    public static VehicleTripResponse mapToVehicleResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());
        var userResponse = UserMapper.response(trip.getCreatedBy());
        var tripResponse = mapToSimpleResponse(trip);

        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new VehicleTripResponse(
                tripResponse,
                routeResponse,
                driverResponse,
                userResponse
        );
    }

    public static RouteTripResponse mapToRouteResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());
        var userResponse = UserMapper.response(trip.getCreatedBy());
        var tripResponse = mapToSimpleResponse(trip);
        var vehicleResponse = VehicleMapper.toResponse(trip.getVehicle());
        return new RouteTripResponse(
                tripResponse,
                vehicleResponse,
                driverResponse,
                userResponse
        );
    }

    public static DriverTripResponse mapToDriverResponse(Trip trip) {

        var userResponse = UserMapper.response(trip.getCreatedBy());
        var tripResponse = mapToSimpleResponse(trip);
        var vehicleResponse = VehicleMapper.toResponse(trip.getVehicle());
        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new DriverTripResponse(
                tripResponse,
                vehicleResponse,
                routeResponse,
                userResponse
        );
    }

    public static TerminalSupervisorTripResponse mapToSupervisorResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());

        var tripResponse = mapToSimpleResponse(trip);
        var vehicleResponse = VehicleMapper.toResponse(trip.getVehicle());
        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new TerminalSupervisorTripResponse(
                tripResponse,
                vehicleResponse,
                routeResponse,
                driverResponse
        );
    }
    public static TripForReservationResponse toResponse(Trip trip) {
        var driverResponse = DriverProfileMapper.response(trip.getDriver());
        var tripResponse = mapToSimpleResponse(trip);

        var routeResponse = RouteMapper.toResponse(trip.getRoute());
        return new TripForReservationResponse(
                tripResponse,
                routeResponse,
                driverResponse
        );
    }

}
