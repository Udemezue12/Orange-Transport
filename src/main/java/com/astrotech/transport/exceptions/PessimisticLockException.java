package com.astrotech.transport.exceptions;

public class PessimisticLockException extends RuntimeException{
    public PessimisticLockException(String message) {
        super(message);
    }
}
