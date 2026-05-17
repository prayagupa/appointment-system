package com.zazzercode.repository;

import com.zazzercode.domain.Provider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {

    Optional<Provider> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Provider> findByTenantId(UUID tenantId);
}
