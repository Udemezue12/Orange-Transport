package com.astrotech.transport.dto.response;

public record AuthFeaturesResponse(
        boolean csrfEnabled,
        boolean webAuthnEnabled,
        boolean corsEnabled,
        boolean oAuth2Enabled,
        boolean registerCodeForAdminEnabled
) {
}
