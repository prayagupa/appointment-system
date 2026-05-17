package com.pratyabhi.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record BookAppointmentRequest(
        @NotNull UUID providerId,
        @NotBlank String patientRef,
        @NotNull Instant startTime,
        @Min(1) int durationMinutes) {}
