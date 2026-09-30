package com.astrotech.transport.dto.request;

import java.util.UUID;

public record TerminalUpdateRequest(


        String terminalName,

        String state,

        String city,
        String address,

        UUID supervisorId
) {
}