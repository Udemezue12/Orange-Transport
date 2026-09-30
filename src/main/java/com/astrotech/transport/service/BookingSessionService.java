package com.astrotech.transport.service;

import com.astrotech.transport.core.AppBuilders;
import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.CreateBookingSessionRequest;
import com.astrotech.transport.dto.request.PassengerSeatRequest;
import com.astrotech.transport.dto.response.BookingSessionResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionWithoutSeatsResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.entities.Seat;
import com.astrotech.transport.entities.TripSeatReservation;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.BookingSessionMapper;
import com.astrotech.transport.mappers.TripSeatReservationMapper;
import com.astrotech.transport.repositories.BookingSessionRepository;
import com.astrotech.transport.repositories.RouteFareRepository;
import com.astrotech.transport.util.CodeGenerator;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.astrotech.transport.mappers.BookingSessionMapper.getBookingSessionResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSessionService {

    private static final int SESSION_EXPIRATION_MINUTES = 15;
    private final BookingSessionRepository bookingSessionRepository;
    private final ProfileService profileService;
    private final TripService tripService;

    private final CodeGenerator referenceGenerator;
    private final TripSeatReservationService reservationService;
    private final SeatService seatService;
    private final RouteFareRepository routeFareRepository;

    @Transactional
    @RoleRequired({
            UserRole.PASSENGER,
            UserRole.CASHIER
    })
    @Caching(evict =
            {
                    @CacheEvict(value = "user-bookings",
                            key = "#currentUserId"),
                    @CacheEvict(value = "booking", allEntries = true),
                    @CacheEvict(value = "all-bookings",
                            allEntries = true)
            }
    )
    public SimpleBookingSessionResponse createSession(
            CreateBookingSessionRequest request,
            UUID currentUserId
    ) {

        var now = Instant.now();

        var trip = tripService.getLockedTrip(request.tripId());

        if (trip.getStatus() != TripStatus.SCHEDULED
                && trip.getStatus() != TripStatus.BOARDING) {
            throw new BadRequestException("Trip is not available for booking.");
        }


        var profile = profileService.getUserProfile(currentUserId);
        var booker = profile.getUser();
        var identityDocument = profile.getIdentityDocument();
        if (booker.getRole() != UserRole.PASSENGER
                && identityDocument.getVerificationStatus() != VerificationStatus.APPROVED
        ){
            throw new BadRequestException("Your profile is yet to be approved by the management");
        }

        var seatIds = request.passengers()
                .stream()
                .map(PassengerSeatRequest::seatId)
                .toList();

        var requestedSeats = seatService.getLockSeats(
                trip.getVehicle().getId(),
                seatIds,
                SeatStatus.AVAILABLE
        );

        if (requestedSeats.size() != seatIds.size()) {
            throw new BadRequestException(
                    "One or more provided seat IDs are invalid or do not belong to this trip's vehicle."
            );
        }

        var seatsAlreadyLocked =
                reservationService.checkIfReservationExists(
                        trip.getId(),
                        seatIds
                );

        if (seatsAlreadyLocked) {
            throw new ConflictException(
                    "One or more selected seats are already reserved or locked by another user."
            );
        }

        var vehicleClass = trip.getVehicle().getVehicleClass();
        var routeId = trip.getRoute().getId();

        var seatUnitPrice = routeFareRepository
                .findActiveFare(routeId, vehicleClass, now, true)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No active fare configured for route "
                                        + routeId
                                        + " and vehicle class "
                                        + vehicleClass
                        )
                );

        var totalAmount = seatUnitPrice.multiply(
                BigDecimal.valueOf(seatIds.size())
        );

        var expiresAt = now.plus(
                SESSION_EXPIRATION_MINUTES,
                ChronoUnit.MINUTES
        );

        var sessionRef =
                referenceGenerator.generateUniqueTripCode();

        var session = BookingSessionMapper.createBooking(
                sessionRef,
                trip,
                booker,
                totalAmount,
                expiresAt,
                now
        );


        var reservations = request.passengers()
                .stream()
                .map(passengerRequest -> {

                    var seat = requestedSeats.stream()
                            .filter(s ->
                                    s.getId().equals(passengerRequest.seatId())
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Seat not found"
                                    )
                            );
                    var trimmedFirstName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(passengerRequest.passengerFirstName(), false);
                    var trimmedLastName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(passengerRequest.passengerLastName(), false);
                    var fullName = AppBuilders.joinStrings(trimmedFirstName, null, trimmedLastName);
                    return TripSeatReservationMapper.createReservation(
                            trip,
                            seat,
                            session,
                            fullName
                    );
                })
                .toList();

        seatService.updateMultipleSeatStatuses(
                seatIds,
                SeatStatus.HELD
        );

        session.getReservations().addAll(reservations);

        var savedSession =
                bookingSessionRepository.save(session);

        return getBookingSessionResponse(
                savedSession,
                seatIds
        );
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "bookings", allEntries = true),
            @CacheEvict(value = "booking", allEntries = true),
            @CacheEvict(value = "user-bookings", allEntries = true),
            @CacheEvict(value = "all-bookings", allEntries = true),
    }
    )
    public SimpleBookingSessionResponse updateSessionStatus(UUID sessionId, BookingSessionStatus sessionStatus) {
        var session = getBookingSession(sessionId);

        if (session.getStatus() == BookingSessionStatus.COMPLETED || session.getStatus() == BookingSessionStatus.EXPIRED) {
            throw new BadRequestException("Cannot update status of a " + session.getStatus() + " session.");
        }


        session.setStatus(sessionStatus);
        List<TripSeatReservation> updatedReservations;
        List<UUID> seatIds;


        if (sessionStatus == BookingSessionStatus.PAYMENT_PROCESSED) {
            updatedReservations = reservationService.updateReservationStatuses(session.getId(), ReservationStatus.BOOKED
            );
            seatIds = reservationService.getSeatIds(updatedReservations);
            seatService.updateMultipleSeatStatuses(seatIds, SeatStatus.BOOKED);
        } else if (sessionStatus == BookingSessionStatus.EXPIRED || sessionStatus == BookingSessionStatus.CANCELLED) {
            updatedReservations = reservationService.updateReservationStatuses(session.getId(), ReservationStatus.CANCELLED);
            seatIds = reservationService.getSeatIds(updatedReservations);
            seatService.updateMultipleSeatStatuses(seatIds, SeatStatus.AVAILABLE);

        } else if (sessionStatus == BookingSessionStatus.PAYMENT_PENDING) {
            updatedReservations = reservationService.updateReservationStatuses(session.getId(), ReservationStatus.PAYMENT_PENDING
            );
            seatIds = reservationService.getSeatIds(updatedReservations);
            seatService.updateMultipleSeatStatuses(seatIds, SeatStatus.PAYMENT_PENDING);

        } else {

            updatedReservations = reservationService.findByBookingSessionId(session.getId());
            seatIds = reservationService.getSeatIds(updatedReservations);
        }


        return getBookingSessionResponse(session, seatIds);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(value = "booking", key = "#bookingReference", ttl = 300, timeUnit = TimeUnit.SECONDS)
    public BookingSessionResponse getBooking(String bookingReference) {
        var trimmedBookingReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(bookingReference, true);
        var session = bookingSessionRepository.findByReferenceWithDetails(trimmedBookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        var seats = getSeatList(session.getId());

        return BookingSessionMapper.mapToResponse(session, seats);
    }


    @Transactional(readOnly = true)
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN})
    @CustomCacheable(value = "bookings", key = "#bookingReference + #userId", ttl = 300, timeUnit = TimeUnit.SECONDS)
    public BookingSessionResponse getUserBooking(String bookingReference, UUID userId) {
        var trimmedBookingReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(bookingReference, true);
        var session = bookingSessionRepository.findBySessionReferenceAndPassengerIdWithDetails(trimmedBookingReference, userId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        var seats = getSeatList(session.getId());
        return BookingSessionMapper.mapToResponse(session, seats);
    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN})
    @CustomCacheable(
            value = "user-bookings",
            key = "#userId + '-p' + #page + '-s' + #size",
            ttl = 60,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleBookingSessionWithoutSeatsResponse> getUserBookings(UUID userId, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "status", true, BookingSession.class, true);
        var result = bookingSessionRepository.findByPassengerIdWithDetails(userId, pageable);


        return getSimpleBookingSessionWithoutSeatsResponseSliceResponse(result);

    }


    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN})
    @CustomCacheable(
            value = "all-bookings",
            key = "'page-' + #page + '-size-' + #size",
            ttl = 30,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleBookingSessionWithoutSeatsResponse> getAllBookings(int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "status", true, BookingSession.class, true);
        var result = bookingSessionRepository.findAllWithDetails(pageable);


        return getSimpleBookingSessionWithoutSeatsResponseSliceResponse(result);

    }

    @Transactional(readOnly = true)
    @CustomCacheable(value = "booking", key = "#bookingId", ttl = 300, timeUnit = TimeUnit.SECONDS)
    public BookingSessionResponse getBookingById(UUID bookingId) {

        var session = getBookingSession(bookingId);

        var seats = getSeatList(session.getId());

        return BookingSessionMapper.mapToResponse(session, seats);
    }

    public BookingSession getBookingSession(UUID sessionId) {
        return bookingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking session not found: "));
    }

    public BookingSession getBookingSessionTripCode(String bookingReference) {
        var trimmedBookingReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(bookingReference, true);
        return bookingSessionRepository.findByReference(trimmedBookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking session not found: " + bookingReference));
    }

    public BookingSession getlockedBookingSession(UUID sessionId) {
        return bookingSessionRepository.findByIdForUpdate(sessionId).orElseThrow(() -> new ResourceNotFoundException("Booking session not found: " + sessionId));
    }

    public List<Seat> getSeatList(UUID sessionId) {
        return reservationService.findByBookingSessionId(sessionId)
                .stream()
                .map(TripSeatReservation::getSeat)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "bookings", allEntries = true),
            @CacheEvict(value = "booking", allEntries = true),
            @CacheEvict(value = "user-bookings", allEntries = true),
            @CacheEvict(value = "all-bookings", allEntries = true)
    })
    public void expirePendingSessionsOlderThanTenMinutes() {
        var tenMinutesAgo = Instant.now().minus(10, ChronoUnit.MINUTES);


        var pendingSessions = bookingSessionRepository
                .findAllByStatusAndCreatedAtBefore(BookingSessionStatus.PENDING, tenMinutesAgo);

        if (pendingSessions.isEmpty()) {
            log.info("JobRunr task: No expired pending sessions to process.");
            return;
        }


        var updatedCount = bookingSessionRepository.updateStatusByCurrentStatusAndCreatedAtBefore(
                BookingSessionStatus.PENDING,
                BookingSessionStatus.EXPIRED,
                tenMinutesAgo
        );


        for (BookingSession session : pendingSessions) {
            var updatedReservations = reservationService.updateReservationStatuses(session.getId(), ReservationStatus.EXPIRED);
            var seatIds = reservationService.getSeatIds(updatedReservations);
            seatService.updateMultipleSeatStatuses(seatIds, SeatStatus.AVAILABLE);
        }


        log.info("JobRunr task: Expired {} pending sessions and released their seat reservations.", updatedCount);
    }

    public BookingSession getBookingSession(String bookingReference) {
        var bookingSession = getBookingSessionTripCode(bookingReference);
        if (bookingSession.getStatus() == BookingSessionStatus.CANCELLED || bookingSession.getStatus() == BookingSessionStatus.EXPIRED) {
            throw new BadRequestException(
                    "Your booking has been canceled or has expired, create a new Booking");
        }
        if (bookingSession.getStatus() == BookingSessionStatus.COMPLETED) {

            throw new BadRequestException(
                    "Booking has already been Paid For");
        }
        return bookingSession;
    }

    private static @NonNull SliceResponse<SimpleBookingSessionWithoutSeatsResponse> getSimpleBookingSessionWithoutSeatsResponseSliceResponse(Slice<BookingSession> result) {
        var content = result.getContent()
                .stream()
                .map(BookingSessionMapper::getBookingSessionWithoutSeatsResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }


}
