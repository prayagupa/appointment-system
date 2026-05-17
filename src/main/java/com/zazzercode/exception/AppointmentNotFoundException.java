package com.zazzercode.exception;

import java.util.UUID;

public class AppointmentNotFoundException extends DomainException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("APPOINTMENT_NOT_FOUND", "Appointment not found: " + appointmentId);
    }
}
