package com.pratyabhi.service;

import com.pratyabhi.domain.Appointment;
import com.pratyabhi.domain.AppointmentStatus;
import com.pratyabhi.exception.AppointmentNotFoundException;
import com.pratyabhi.exception.InvalidArgumentException;
import com.pratyabhi.repository.AppointmentRepository;
import com.pratyabhi.tenancy.TenantContext;
import java.time.Instant;
import java.util.List;
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
            throw new com.pratyabhi.exception.SlotUnavailableException(
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

    @Transactional(readOnly = true)
    public List<Appointment> list(UUID providerId) {
        UUID tenantId = TenantContext.getRequired();
        tenantService.ensureExists(tenantId);
        if (providerId == null) {
            return appointmentRepository.findByTenantIdOrderByStartTimeDesc(tenantId);
        }
        return appointmentRepository.findByTenantIdAndProviderIdOrderByStartTimeDesc(tenantId, providerId);
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
