package com.astrotech.transport.dto.response;


public record TerminalResponse(
        SimpleTerminalResponse terminal,
        UserResponse supervisor
) {


}
