package com.zazzercode.exception;

public class SlotUnavailableException extends DomainException {

    public SlotUnavailableException(String message) {
        super("SLOT_UNAVAILABLE", message);
    }
}
