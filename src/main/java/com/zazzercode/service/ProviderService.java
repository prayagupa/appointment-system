package com.zazzercode.service;

import com.zazzercode.domain.Provider;
import com.zazzercode.exception.ProviderNotFoundException;
import com.zazzercode.repository.ProviderRepository;
import com.zazzercode.tenancy.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final TenantService tenantService;

    public ProviderService(ProviderRepository providerRepository, TenantService tenantService) {
        this.providerRepository = providerRepository;
        this.tenantService = tenantService;
    }

    public Provider get(UUID providerId) {
        UUID tenantId = TenantContext.getRequired();
        tenantService.ensureExists(tenantId);
        return providerRepository
                .findByIdAndTenantId(providerId, tenantId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
    }

    public List<Provider> listByTenant() {
        UUID tenantId = TenantContext.getRequired();
        tenantService.ensureExists(tenantId);
        return providerRepository.findByTenantId(tenantId);
    }
}
