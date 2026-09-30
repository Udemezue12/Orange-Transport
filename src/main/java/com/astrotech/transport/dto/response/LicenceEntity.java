package com.astrotech.transport.dto.response;

public record LicenceEntity(
        String uuid,
        String licenseNo,
        String firstName,
        String lastName,
        String middleName,
        String gender,
        String issuedDate,
        String expiryDate,
        String stateOfIssue,
        String birthDate,
        String photo
) {}
