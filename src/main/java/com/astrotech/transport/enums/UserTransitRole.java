package com.astrotech.transport.enums;

public enum UserTransitRole {
    PASSENGER,
    WORKER,
    CONDUCTOR,
    DRIVER,
    SUPERVISOR,
    ADMIN;
    public boolean requiresProfile() {
        return this == PASSENGER || this == SUPERVISOR || this == ADMIN;
    }
  
}
