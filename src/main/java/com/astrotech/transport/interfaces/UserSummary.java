package com.astrotech.transport.interfaces;

import java.util.UUID;

public interface UserSummary {
    UUID getId();

    String getName();

    String getEmail();

    Boolean getIsActive();

}
