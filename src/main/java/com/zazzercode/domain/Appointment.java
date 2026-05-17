package com.zazzercode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "patient_ref", nullable = false)
    private String patientRef;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status;

    @Version
    private long version;

    protected Appointment() {
    }

    public Appointment(
            UUID id,
            UUID tenantId,
            UUID providerId,
            String patientRef,
            Instant startTime,
            int durationMinutes,
            AppointmentStatus status) {
        this.id = id;
        this.tenantId = tenantId;
        this.providerId = providerId;
        this.patientRef = patientRef;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getPatientRef() {
        return patientRef;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public Instant getEndTime() {
        return startTime.plusSeconds(durationMinutes * 60L);
    }
}
