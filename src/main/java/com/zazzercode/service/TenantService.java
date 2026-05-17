package com.zazzercode.service;

import com.zazzercode.domain.Tenant;
import com.zazzercode.exception.TenantNotFoundException;
import com.zazzercode.repository.TenantRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant get(UUID tenantId) {
        return tenantRepository.findById(tenantId).orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    public void ensureExists(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
    }
}
