package com.astrotech.transport.dto.response;

public record TripAssignedWorkerResponse(
        SimpleAssignedWorkerResponse assignedWorkerResponse,
        UserResponse assignedByResponse,
        UserResponse worker,
        SimpleTripResponse tripResponse
        
){}
