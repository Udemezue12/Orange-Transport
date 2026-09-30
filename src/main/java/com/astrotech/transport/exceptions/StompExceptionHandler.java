package com.astrotech.transport.exceptions;


public class StompExceptionHandler extends  RuntimeException {
    public StompExceptionHandler(String message) {
        super(message);
    }
}

