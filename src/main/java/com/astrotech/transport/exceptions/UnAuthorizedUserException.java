package com.astrotech.transport.exceptions;

public class UnAuthorizedUserException extends RuntimeException {
    public UnAuthorizedUserException(String message) {
        super(message);
    }
}
