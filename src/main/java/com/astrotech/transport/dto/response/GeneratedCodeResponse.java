package com.astrotech.transport.dto.response;

import com.astrotech.transport.core.*;
import com.astrotech.transport.enums.UserRole;
import com.fasterxml.jackson.databind.annotation.*;

import java.time.Instant;


public record GeneratedCodeResponse(

        String registerCode,

        UserRole role,

//        @JsonSerialize(using = InstantDateTimeSerializer.class)
        Instant generatedAt

) {
}


