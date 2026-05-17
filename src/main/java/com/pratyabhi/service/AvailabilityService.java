package com.pratyabhi.service;

import com.pratyabhi.domain.Appointment;
import com.pratyabhi.domain.AppointmentStatus;
import com.pratyabhi.domain.Availability;
import com.pratyabhi.exception.InvalidArgumentException;
import com.pratyabhi.exception.ProviderNotFoundException;
import com.pratyabhi.exception.SlotUnavailableException;
import com.pratyabhi.repository.AppointmentRepository;
import com.pratyabhi.repository.AvailabilityRepository;
import com.pratyabhi.repository.ProviderRepository;
import com.pratyabhi.tenancy.TenantContext;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final TenantService tenantService;

    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository,
            ProviderRepository providerRepository,
            TenantService tenantService) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.providerRepository = providerRepository;
        this.tenantService = tenantService;
    }

    public void assertSlotAvailable(UUID providerId, Instant start, int durationMinutes) {
        UUID tenantId = TenantContext.getRequired();
        ensureProvider(tenantId, providerId);

        Instant end = start.plusSeconds(durationMinutes * 60L);
        if (!isWithinAvailability(providerId, start, end)) {
            throw new SlotUnavailableException("Requested time is outside provider availability");
        }
        if (hasOverlap(tenantId, providerId, start, end)) {
            throw new SlotUnavailableException("Requested slot overlaps an existing appointment");
        }
    }

    public List<TimeSlot> listAvailability(
            UUID providerId, Instant rangeStart, Instant rangeEnd, int slotDurationMinutes) {
        if (slotDurationMinutes <= 0) {
            throw new InvalidArgumentException("slot_duration_minutes must be positive");
        }
        if (!rangeEnd.isAfter(rangeStart)) {
            throw new InvalidArgumentException("range_end must be after range_start");
        }

        UUID tenantId = TenantContext.getRequired();
        tenantService.ensureExists(tenantId);
        ensureProvider(tenantId, providerId);

        List<Availability> rules = availabilityRepository.findByProviderId(providerId);
        List<Appointment> booked =
                appointmentRepository.findBookedInRange(tenantId, providerId, rangeStart, rangeEnd);

        List<TimeSlot> slots = new ArrayList<>();
        LocalDate cursor = LocalDate.ofInstant(rangeStart, ZoneOffset.UTC);
        LocalDate endDate = LocalDate.ofInstant(rangeEnd, ZoneOffset.UTC);

        while (!cursor.isAfter(endDate)) {
            int dayOfWeek = cursor.getDayOfWeek().getValue();
            for (Availability rule : rules) {
                if (rule.getDayOfWeek() != dayOfWeek) {
                    continue;
                }
                LocalDateTime windowStart = LocalDateTime.of(cursor, rule.getStartTime());
                LocalDateTime windowEnd = LocalDateTime.of(cursor, rule.getEndTime());
                LocalDateTime slotStart = windowStart;

                while (slotStart.plusMinutes(slotDurationMinutes).compareTo(windowEnd) <= 0) {
                    Instant slotStartInstant = slotStart.toInstant(ZoneOffset.UTC);
                    Instant slotEndInstant = slotStart.plusMinutes(slotDurationMinutes).toInstant(ZoneOffset.UTC);

                    if (slotEndInstant.isAfter(rangeStart)
                            && slotStartInstant.isBefore(rangeEnd)
                            && !overlapsBooked(booked, slotStartInstant, slotEndInstant)) {
                        slots.add(new TimeSlot(slotStartInstant, slotEndInstant));
                    }
                    slotStart = slotStart.plusMinutes(slotDurationMinutes);
                }
            }
            cursor = cursor.plusDays(1);
        }
        return slots;
    }

    private boolean isWithinAvailability(UUID providerId, Instant start, Instant end) {
        LocalDate date = LocalDate.ofInstant(start, ZoneOffset.UTC);
        int dayOfWeek = date.getDayOfWeek().getValue();
        LocalTime startTime = LocalTime.ofInstant(start, ZoneOffset.UTC);
        LocalTime endTime = LocalTime.ofInstant(end, ZoneOffset.UTC);

        return availabilityRepository.findByProviderId(providerId).stream()
                .anyMatch(rule -> rule.getDayOfWeek() == dayOfWeek
                        && !startTime.isBefore(rule.getStartTime())
                        && !endTime.isAfter(rule.getEndTime()));
    }

    private boolean overlapsBooked(List<Appointment> booked, Instant slotStart, Instant slotEnd) {
        for (Appointment appointment : booked) {
            Instant apptEnd = appointment.getEndTime();
            if (appointment.getStartTime().isBefore(slotEnd) && apptEnd.isAfter(slotStart)) {
                return true;
            }
        }
        return false;
    }

    private void ensureProvider(UUID tenantId, UUID providerId) {
        providerRepository
                .findByIdAndTenantId(providerId, tenantId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
    }

    private boolean hasOverlap(UUID tenantId, UUID providerId, Instant start, Instant end) {
        return appointmentRepository
                .findByTenantIdAndProviderIdAndStatus(tenantId, providerId, AppointmentStatus.BOOKED)
                .stream()
                .anyMatch(appointment ->
                        appointment.getStartTime().isBefore(end) && appointment.getEndTime().isAfter(start));
    }

    public record TimeSlot(Instant start, Instant end) {}
}
