package com.astrotech.transport.dto.response;

import java.util.UUID;

public record SimpleTerminalResponse(
        UUID id,
        String terminalName,
        String state,
        String city,
        String address
){

}
