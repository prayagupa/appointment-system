package com.pratyabhi.web.dto;

import com.pratyabhi.domain.Appointment;
import java.time.Instant;
import java.util.UUID;

public record AppointmentDto(
        UUID id,
        UUID tenantId,
        UUID providerId,
        String patientRef,
        Instant startTime,
        Instant endTime,
        int durationMinutes,
        String status) {

    public static AppointmentDto from(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getTenantId(),
                appointment.getProviderId(),
                appointment.getPatientRef(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getDurationMinutes(),
                appointment.getStatus().name());
    }
}
