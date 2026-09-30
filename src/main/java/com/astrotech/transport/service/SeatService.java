package com.astrotech.transport.service;


import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.BulkUpdateSeatsRequest;
import com.astrotech.transport.dto.request.CreateSeatRequest;
import com.astrotech.transport.dto.request.SeatLayout;
import com.astrotech.transport.dto.response.SeatResponse;
import com.astrotech.transport.entities.Seat;
import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.enums.SeatStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.SeatMapper;
import com.astrotech.transport.repositories.SeatRepository;
import com.astrotech.transport.repositories.VehicleRepository;
import com.astrotech.transport.util.SeatPositionResolver;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final VehicleRepository vehicleRepository;

    private List<Seat> getSeats(Integer vehicleCapacity, List<Integer> seatsPerRow, UUID vehicleId) {
        var vehicle = getVehicle(vehicleId);
        int capacity = vehicleCapacity;


        var totalLayoutSeats = (seatsPerRow != null)
                ? seatsPerRow.stream().mapToInt(Integer::intValue).sum()
                : 0;

        if (totalLayoutSeats != capacity) {
            throw new IllegalArgumentException(
                    String.format("Seat layout sum (%d) does not match vehicle capacity (%d)", totalLayoutSeats, capacity)
            );
        }


        List<Seat> seats = new ArrayList<>(capacity);
        int seatCount = 0;

        for (int rowIndex = 0; rowIndex < Objects.requireNonNull(seatsPerRow).size() && seatCount < capacity; rowIndex++) {

            int rowNumber = rowIndex + 1;
            int seatsInRow = seatsPerRow.get(rowIndex);

            for (int i = 0; i < seatsInRow && seatCount < capacity; i++) {

                var seatNumber = rowNumber + String.valueOf((char) ('A' + i));


                var position = SeatPositionResolver.resolve(vehicle.getVehicleType(), i, seatsInRow);


                Seat seat = SeatMapper.createSeat(
                        rowNumber,
                        SeatStatus.AVAILABLE,
                        seatNumber,
                        position,
                        vehicle
                );

                seats.add(seat);
                seatCount++;
            }
        }
        return seats;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicle-seats", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'seats-' + #vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'images-seats-' + #vehicleId")
    })
    public void createVehicleSeat(UUID vehicleId, Integer vehicleCapacity, List<Integer> seatsPerRow) {


        var seats = getSeats(vehicleCapacity, seatsPerRow, vehicleId);


        seatRepository.saveAll(seats);


    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicle-seats", key = "#vehicleId"),
            @CacheEvict(value = "vehicles", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'seats-' + #vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'images-seats-' + #vehicleId")
    })
    public List<SeatResponse> updateVehicleSeats(
            UUID vehicleId,
            Integer vehicleCapacity,
            List<Integer> seatsPerRow
    ) {
        var vehicle = getVehicle(vehicleId);

        var layout = generateSeatLayout(
                vehicle,
                vehicleCapacity,
                seatsPerRow
        );

        var existingSeats =
                seatRepository.findAllByVehicleId(vehicleId);

        var existingByNumber =
                existingSeats.stream()
                        .collect(Collectors.toMap(
                                Seat::getSeatNumber,
                                Function.identity()
                        ));

        var newSeatNumbers =
                layout.stream()
                        .map(SeatLayout::seatNumber)
                        .collect(Collectors.toSet());

        List<Seat> seatsToRemove = existingSeats.stream()
                .filter(seat ->
                        !newSeatNumbers.contains(seat.getSeatNumber())
                )
                .toList();

        if (!seatsToRemove.isEmpty()) {
            throw new BadRequestException(
                    "The new seat layout removes existing seats: " +
                            seatsToRemove.stream()
                                    .map(Seat::getSeatNumber)
                                    .sorted()
                                    .collect(Collectors.joining(", "))
            );
        }

        List<Seat> seatsToSave = new ArrayList<>();

        for (var seatLayout : layout) {

            var existingSeat =
                    existingByNumber.get(seatLayout.seatNumber());

            if (existingSeat != null) {

                existingSeat.setRowNumber(seatLayout.rowNumber());
                existingSeat.setPosition(seatLayout.position());

                seatsToSave.add(existingSeat);

            } else {

                seatsToSave.add(
                        SeatMapper.createSeat(
                                seatLayout.rowNumber(),
                                SeatStatus.AVAILABLE,
                                seatLayout.seatNumber(),
                                seatLayout.position(),
                                vehicle
                        )
                );
            }
        }

        var response = seatRepository.saveAll(seatsToSave);
        return response.stream()
                .map(SeatMapper::toResponse)
                .toList();
    }

    private Vehicle getVehicle(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "vehicle-seats", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'seats-' + #vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "'images-seats-' + #vehicleId")
    })
    public List<SeatResponse> createSeat(UUID vehicleId, CreateSeatRequest request) {


        var seats = getSeats(request.capacity(), request.seatsPerRow(), vehicleId);


        var savedSeats = seatRepository.saveAll(seats);
        return savedSeats.stream()
                .map(SeatMapper::toResponse)
                .toList();


    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    public List<SeatResponse> updateSeats(UUID vehicleId, BulkUpdateSeatsRequest request) {


        return updateVehicleSeats(vehicleId, request.vehicleCapacity(), request.seatsPerRow());
    }

    @Transactional
    @CustomCacheable(
            value = "vehicle-seats",
            key = "#vehicleId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public List<SeatResponse> getVehicleSeats(UUID vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }

        return seatRepository.findByVehicleIdOrderBySeatNumberAsc(vehicleId)
                .stream()
                .map(SeatMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<Seat> getLockSeats(UUID vehicleId, List<UUID> seatIds, SeatStatus seatStatus) {
        return seatRepository.findAllByIdWithPessimisticLock(seatIds, vehicleId, seatStatus);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicle-seats", allEntries = true),
            @CacheEvict(value = "vehicle-details", allEntries = true)
    })
    public void updateMultipleSeatStatuses(List<UUID> seatIds, SeatStatus seatStatus) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }
        seatRepository.updateStatusesForIds(seatIds, seatStatus);
    }

    private List<SeatLayout> generateSeatLayout(
            Vehicle vehicle,
            Integer vehicleCapacity,
            List<Integer> seatsPerRow
    ) {
        if (vehicleCapacity == null || vehicleCapacity <= 0) {
            throw new IllegalArgumentException("Vehicle capacity must be greater than zero");
        }

        if (seatsPerRow == null || seatsPerRow.isEmpty()) {
            throw new IllegalArgumentException("Seat layout cannot be empty");
        }

        int totalLayoutSeats = seatsPerRow.stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalLayoutSeats != vehicleCapacity) {
            throw new IllegalArgumentException(
                    String.format(
                            "Seat layout sum (%d) does not match vehicle capacity (%d)",
                            totalLayoutSeats,
                            vehicleCapacity
                    )
            );
        }

        List<SeatLayout> layout = new ArrayList<>(vehicleCapacity);

        for (int rowIndex = 0; rowIndex < seatsPerRow.size(); rowIndex++) {

            int rowNumber = rowIndex + 1;
            int seatsInRow = seatsPerRow.get(rowIndex);

            for (int i = 0; i < seatsInRow; i++) {

                String seatNumber =
                        rowNumber + String.valueOf((char) ('A' + i));

                var position =
                        SeatPositionResolver.resolve(
                                vehicle.getVehicleType(),
                                i,
                                seatsInRow
                        );

                layout.add(
                        new SeatLayout(
                                rowNumber,
                                seatNumber,
                                position
                        )
                );
            }
        }

        return layout;
    }

}
