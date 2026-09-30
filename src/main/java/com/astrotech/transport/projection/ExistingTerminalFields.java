package com.astrotech.transport.projection;

import com.astrotech.transport.entities.User;

import java.util.UUID;

public interface ExistingTerminalFields {
    String getName();
    String getAddress();
    UUID getSupervisorId();
}
