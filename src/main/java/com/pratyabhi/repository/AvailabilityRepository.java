package com.pratyabhi.repository;

import com.pratyabhi.domain.Availability;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    List<Availability> findByProviderId(UUID providerId);
}
