package com.astrotech.transport.exceptions;

public class UnAuthenticatedUserException extends RuntimeException {
    public UnAuthenticatedUserException(String message) {
        super(message);
    }
}
