package com.pratyabhi.grpc;

import com.google.protobuf.Timestamp;
import com.pratyabhi.domain.Appointment;
import com.pratyabhi.domain.Provider;
import com.pratyabhi.service.AvailabilityService;
import java.time.Instant;

public final class ProtoMapper {

    private ProtoMapper() {
    }

    public static com.pratyabhi.grpc.appointment.v1.Provider toProvider(Provider provider) {
        return com.pratyabhi.grpc.appointment.v1.Provider.newBuilder()
                .setId(provider.getId().toString())
                .setTenantId(provider.getTenantId().toString())
                .setDisplayName(provider.getDisplayName())
                .setSpecialty(provider.getSpecialty() == null ? "" : provider.getSpecialty())
                .build();
    }

    public static com.pratyabhi.grpc.appointment.v1.Appointment toAppointment(Appointment appointment) {
        return com.pratyabhi.grpc.appointment.v1.Appointment.newBuilder()
                .setId(appointment.getId().toString())
                .setTenantId(appointment.getTenantId().toString())
                .setProviderId(appointment.getProviderId().toString())
                .setPatientRef(appointment.getPatientRef())
                .setStartTime(toTimestamp(appointment.getStartTime()))
                .setDurationMinutes(appointment.getDurationMinutes())
                .setStatus(appointment.getStatus().name())
                .build();
    }

    public static com.pratyabhi.grpc.appointment.v1.TimeSlot toTimeSlot(AvailabilityService.TimeSlot slot) {
        return com.pratyabhi.grpc.appointment.v1.TimeSlot.newBuilder()
                .setStartTime(toTimestamp(slot.start()))
                .setEndTime(toTimestamp(slot.end()))
                .build();
    }

    public static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
