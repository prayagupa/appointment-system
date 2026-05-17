package com.zazzercode.exception;

import java.util.UUID;

public class TenantNotFoundException extends DomainException {

    public TenantNotFoundException(UUID tenantId) {
        super("TENANT_NOT_FOUND", "Tenant not found: " + tenantId);
    }
}
