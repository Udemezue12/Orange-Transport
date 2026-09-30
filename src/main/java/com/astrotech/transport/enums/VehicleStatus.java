package com.astrotech.transport.enums;

public enum VehicleStatus {

    AVAILABLE,

    ON_TRIP,

    BOARDING,
    ASSIGNED,

    RESERVED,

    UNLOADING,
    ON_HOLD,

    UNDER_MAINTENANCE,

    OUT_OF_SERVICE,

    RETIRED;

    public boolean canParticipateInTransloading() {
        return switch (this) {
            case BOARDING,
                 ON_HOLD,
                 UNDER_MAINTENANCE,
                 OUT_OF_SERVICE,
                 RETIRED -> false;

            default -> true;
        };
    }
    public boolean canParticipateInTrip() {
        return switch (this) {
            case AVAILABLE,
                 UNLOADING-> true;

            default -> false;
        };
    }
}
