package com.zazzercode.repository;

import com.zazzercode.domain.Appointment;
import com.zazzercode.domain.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            select a from Appointment a
            where a.tenantId = :tenantId
              and a.providerId = :providerId
              and a.status = com.zazzercode.domain.AppointmentStatus.BOOKED
              and a.startTime < :end
              and a.startTime >= :rangeStart
            """)
    List<Appointment> findBookedInRange(
            @Param("tenantId") UUID tenantId,
            @Param("providerId") UUID providerId,
            @Param("rangeStart") Instant rangeStart,
            @Param("end") Instant end);

    List<Appointment> findByTenantIdAndProviderIdAndStatus(
            UUID tenantId, UUID providerId, AppointmentStatus status);
}
