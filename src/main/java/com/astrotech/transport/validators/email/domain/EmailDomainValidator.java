package com.astrotech.transport.validators.email.domain;

import com.astrotech.transport.configProperties.AllowedOrigins;
import com.astrotech.transport.configProperties.EncryptionProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class EmailDomainValidator implements ConstraintValidator<ValidateEmailDomains, String> {
    private final AllowedOrigins allowedOrigins;
    private final EncryptionProperties encryptionProperties;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        if (email == null || !email.contains("@")) {
            return true;
        }
        if (!encryptionProperties.productionMode()) {
            return true;
        }
        if (allowedOrigins == null ||
                allowedOrigins.allowedEmailDomains() == null ||
                allowedOrigins.allowedEmailDomains().isEmpty()) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase(Locale.ROOT);
        return allowedOrigins
                .allowedEmailDomains()
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(allowedDomain -> allowedDomain.toLowerCase(Locale.ROOT))
                .anyMatch(domain::equals);

    }
}
