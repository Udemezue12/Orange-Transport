package com.astrotech.transport.service;

import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.SimpleTripSeatReservationResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TripSeatReservationWithBookingSessionResponse;
import com.astrotech.transport.dto.response.TripSeatReservationWithoutBookingSessionResponse;
import com.astrotech.transport.entities.Seat;
import com.astrotech.transport.entities.TripSeatReservation;
import com.astrotech.transport.enums.ReservationStatus;
import com.astrotech.transport.enums.SeatStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.ForbiddenException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.TripSeatReservationMapper;
import com.astrotech.transport.repositories.BookingSessionRepository;
import com.astrotech.transport.repositories.TripSeatReservationRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TripSeatReservationService {
    private final TripSeatReservationRepository tripSeatReservationRepository;
    private final BookingSessionRepository bookingSessionRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservations", allEntries = true),
            @CacheEvict(value = "reservation-lists", allEntries = true),
            @CacheEvict(value = "trip-lists", allEntries = true)
    })
    public List<TripSeatReservation> updateReservationStatuses(UUID sessionId, ReservationStatus targetStatus) {
        var reservations = getByBookingSessionId(sessionId);


        reservations.forEach(res -> res.setStatus(targetStatus));

        return tripSeatReservationRepository.saveAll(reservations);
    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN})
    @CustomCacheable(
            value = "reservation-lists",
            key = "'user-' + #userId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripSeatReservationResponse> getAllUserReservations(UUID userId, int page, int size, String sortBy) {
        var session = bookingSessionRepository.findByPassengerId(userId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TripSeatReservation.class, true);
        var result = tripSeatReservationRepository.findAllByBookingSessionId(session.getId(), pageable);
        var content = result
                .getContent()
                .stream()
                .map(TripSeatReservationMapper::simpleSeatReservationResponse)
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
    @RoleRequired({UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "reservation-lists",
            key = "'trip-' + #tripId + '-user-' + #userId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripSeatReservationResponse> getAllTripReservations(UUID tripId, UUID userId, int page, int size, String sortBy) {
        var session = bookingSessionRepository.findByTripId(tripId).orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        var supervisor = session.getTrip()
                .getRoute()
                .getOriginTerminal()
                .getTerminalSupervisor();

        if (!supervisor.getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to view this trip");
        }

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TripSeatReservation.class, true);
        var result = tripSeatReservationRepository.findAllByBookingSessionId(session.getId(), pageable);
        var content = result
                .getContent()
                .stream()
                .map(TripSeatReservationMapper::simpleSeatReservationResponse)
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
    @RoleRequired({UserRole.ADMIN})
    @CustomCacheable(
            value = "reservation-lists",
            key = "'all-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 60,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripSeatReservationResponse> getAllReservations(int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, TripSeatReservation.class, true);
        var result = tripSeatReservationRepository.findAllBy(pageable);
        var content = result
                .getContent()
                .stream()
                .map(TripSeatReservationMapper::simpleSeatReservationResponse)
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
    @RoleRequired({UserRole.ADMIN, UserRole.DRIVER, UserRole.PASSENGER, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "reservations",
            key = "'no-booking-' + #reservationId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TripSeatReservationWithoutBookingSessionResponse getReservationWithoutBooking(UUID reservationId) {
        return tripSeatReservationRepository.findById(reservationId)
                .map(TripSeatReservationMapper::mapFromResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));


    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN})
    @CustomCacheable(
            value = "reservations",
            key = "'with-booking-' + #reservationId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TripSeatReservationWithBookingSessionResponse getReservationWithBooking(UUID reservationId) {
        var tripReservation = tripSeatReservationRepository.findByIdWithFullSessionDetails(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        var reservationTrip = tripReservation.getTrip();

        var seats = (reservationTrip != null && reservationTrip.getSeatReservations() != null)
                ? getSeats(reservationTrip.getSeatReservations())
                : List.<Seat>of();

        return TripSeatReservationMapper.mapToResponse(tripReservation, seats);
    }


    public List<TripSeatReservation> getByBookingSessionId(UUID sessionId) {
        return tripSeatReservationRepository.findByBookingSessionId(sessionId);
    }

    public boolean checkIfReservationExists(UUID tripId, List<UUID> seatIds) {
        return tripSeatReservationRepository.existsActiveReservationsForTripAndSeats(
                tripId,
                seatIds,
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public List<TripSeatReservation> findByBookingSessionId(UUID sessionId) {
        if (sessionId == null) {
            return List.of();
        }
        return getByBookingSessionId(sessionId);
    }

    public List<UUID> getSeatIds(List<TripSeatReservation> reservations) {
        return reservations
                .stream()
                .map(res -> res.getSeat().getId())
                .toList();
    }

    public List<Seat> getSeats(List<TripSeatReservation> updatedReservations) {
        return updatedReservations
                .stream()
                .map(TripSeatReservation::getSeat)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservations", key = "'no-booking-' + #reservation.id"),
            @CacheEvict(value = "reservations", key = "'with-booking-' + #reservation.id"),
            @CacheEvict(value = "reservation-lists", allEntries = true),
            @CacheEvict(value = "trip-lists", allEntries = true)
    })
    public void updateReservationStatus(
            TripSeatReservation reservation,
            ReservationStatus status
    ) {
        reservation.setStatus(status);

        var seat = reservation.getSeat();

        seat.setStatus(getSeatStatus(status));
    }

    private SeatStatus getSeatStatus(ReservationStatus status) {
        return switch (status) {
            case AVAILABLE, CANCELLED, EXPIRED -> SeatStatus.AVAILABLE;
            case HELD -> SeatStatus.HELD;
            case PAYMENT_PENDING -> SeatStatus.PAYMENT_PENDING;
            case BOOKED, CONFIRMED -> SeatStatus.BOOKED;
            case CHECKED_IN -> SeatStatus.CHECKED_IN;
        };
    }

}
