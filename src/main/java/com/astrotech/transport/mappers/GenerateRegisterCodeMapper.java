package com.astrotech.transport.mappers;

import com.astrotech.transport.core.AppGenerators;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.response.GeneratedCodeResponse;
import com.astrotech.transport.entities.RegistrationCode;
import com.astrotech.transport.enums.CodeStatus;
import com.astrotech.transport.enums.UserRole;

import java.time.Instant;

public class GenerateRegisterCodeMapper {
    public static RegistrationCode generateCode(String trimmedCode, UserRole role){

        return RegistrationCode
                .builder()
                .role(role)
                .status(CodeStatus.VALID)
                .generatedAt(Instant.now())
                .registerCode(trimmedCode)
                .build();
    }
    public static GeneratedCodeResponse getResponse(RegistrationCode registrationCode){
        return new GeneratedCodeResponse(
                registrationCode.getRegisterCode(),
                registrationCode.getRole(),
                registrationCode.getGeneratedAt()
        );
    }
}
