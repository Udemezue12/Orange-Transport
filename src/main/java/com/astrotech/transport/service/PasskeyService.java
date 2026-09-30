package com.astrotech.transport.service;

import com.astrotech.transport.dto.response.PasskeyResponse;
import com.astrotech.transport.mappers.PasskeyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasskeyService {

    private final JdbcUserCredentialRepository credentialRepository;
    private final JdbcPublicKeyCredentialUserEntityRepository userEntityRepository;
    @Transactional(readOnly = true)

    public List<PasskeyResponse> findUserPasskeys(
            String username
    ) {

        var userEntity =
                userEntityRepository.findByUsername(username);

        if (userEntity == null) {
            return List.of();
        }

        return credentialRepository
                .findByUserId(userEntity.getId())
                .stream()
                .map(credential ->
                        PasskeyMapper.passkeyResponse(
                                credential.getCredentialId().toBase64UrlString(),
                                credential.getLabel(),
                                credential.getCreated(),
                                credential.getLastUsed()
                        )
                )
                .toList();
    }
}
