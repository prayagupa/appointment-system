package com.zazzercode.exception;

public class InvalidArgumentException extends DomainException {

    public InvalidArgumentException(String message) {
        super("INVALID_ARGUMENT", message);
    }
}
