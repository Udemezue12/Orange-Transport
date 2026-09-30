package com.astrotech.transport.webauthn;

public interface WebAuthnEndpoints {
    String PASSKEY_LOGIN_OPTIONS = "/api/v1/auth/passkeys/login/options";
    String PASSKEY_LOGIN_VERIFY = "/api/v1/auth/passkeys/login/verify";

    String PASSKEY_REGISTER_OPTIONS = "/api/v1/auth/passkeys/register/options";
    String PASSKEY_REGISTER_VERIFY = "/api/v1/auth/passkeys/register/verify";

}
