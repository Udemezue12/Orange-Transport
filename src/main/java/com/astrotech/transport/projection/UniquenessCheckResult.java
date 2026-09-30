package com.astrotech.transport.projection;

public interface UniquenessCheckResult {
    boolean isEmailExists();
    boolean isNickNameExists();
    boolean isPhoneExists();
}
