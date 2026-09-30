package com.astrotech.transport.dto.response;

public record TerminalAssignedWorkerResponse(
        SimpleAssignedWorkerResponse assignedWorkerResponse,
        UserResponse assignedByResponse,
        UserResponse worker,
        SimpleTerminalResponse terminalResponse

){}
