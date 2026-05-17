package com.zazzercode.service;

import com.zazzercode.domain.Appointment;
import com.zazzercode.domain.AppointmentStatus;
import com.zazzercode.exception.AppointmentNotFoundException;
import com.zazzercode.exception.InvalidArgumentException;
import com.zazzercode.repository.AppointmentRepository;
import com.zazzercode.tenancy.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;
    private final TenantService tenantService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailabilityService availabilityService,
            TenantService tenantService) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityService = availabilityService;
        this.tenantService = tenantService;
    }

    public Appointment book(UUID providerId, String patientRef, Instant startTime, int durationMinutes) {
        validateBooking(providerId, patientRef, startTime, durationMinutes);

        UUID tenantId = TenantContext.getRequired();
        tenantService.ensureExists(tenantId);
        availabilityService.assertSlotAvailable(providerId, startTime, durationMinutes);

        Appointment appointment = new Appointment(
                UUID.randomUUID(),
                tenantId,
                providerId,
                patientRef,
                startTime,
                durationMinutes,
                AppointmentStatus.BOOKED);

        try {
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new com.zazzercode.exception.SlotUnavailableException(
                    "Requested slot is no longer available");
        }
    }

    public Appointment cancel(UUID appointmentId) {
        Appointment appointment = findById(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
            appointment.cancel();
        }
        return appointment;
    }

    @Transactional(readOnly = true)
    public Appointment findById(UUID appointmentId) {
        UUID tenantId = TenantContext.getRequired();
        return appointmentRepository
                .findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    private void validateBooking(UUID providerId, String patientRef, Instant startTime, int durationMinutes) {
        if (providerId == null) {
            throw new InvalidArgumentException("provider_id is required");
        }
        if (patientRef == null || patientRef.isBlank()) {
            throw new InvalidArgumentException("patient_ref is required");
        }
        if (startTime == null) {
            throw new InvalidArgumentException("start_time is required");
        }
        if (durationMinutes <= 0) {
            throw new InvalidArgumentException("duration_minutes must be positive");
        }
    }
}
