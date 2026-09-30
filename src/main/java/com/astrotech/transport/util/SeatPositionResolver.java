package com.astrotech.transport.util;

import com.astrotech.transport.enums.SeatPosition;
import com.astrotech.transport.enums.VehicleType;

public class SeatPositionResolver {

    public static SeatPosition resolve(VehicleType vehicleType, int positionInRow, int seatsPerRow) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        return switch (vehicleType) {

            case SIENNA, SUV, VAN, PICKUP -> resolveSmallVehicle(positionInRow, seatsPerRow);


            case HIACE, COASTER, MINI_BUS, MIDI_BUS, CITY_BUS,
                 LUXURY_BUS, EXECUTIVE_BUS, SLEEPER_BUS, DOUBLE_DECKER_BUS,
                 HUMMER_BUS, SPRINTER, SHUTTLE_BUS, TRUCK -> resolveAisleVehicle(positionInRow, seatsPerRow);
        };
    }


    private static SeatPosition resolveSmallVehicle(int positionInRow, int seatsPerRow) {
        if (seatsPerRow <= 1) {
            return SeatPosition.WINDOW;
        }
        if (positionInRow == 0 || positionInRow == seatsPerRow - 1) {
            return SeatPosition.WINDOW;
        }
        return SeatPosition.MIDDLE;
    }

    private static SeatPosition resolveAisleVehicle(int positionInRow, int seatsPerRow) {
        if (seatsPerRow <= 1) {
            return SeatPosition.WINDOW;
        }


        if (positionInRow == 0 || positionInRow == seatsPerRow - 1) {
            return SeatPosition.WINDOW;
        }


        int middleIndex = seatsPerRow / 2;


        if (positionInRow == middleIndex - 1 || positionInRow == middleIndex) {
            return SeatPosition.AISLE;
        }


        return SeatPosition.MIDDLE;
    }
}
