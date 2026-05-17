package com.zazzercode.tenancy;

import io.grpc.Context;
import java.util.UUID;

public final class TenantContext {

    private static final Context.Key<UUID> TENANT_ID_KEY = Context.key("tenant-id");

    private TenantContext() {
    }

    public static Context attach(UUID tenantId) {
        return Context.current().withValue(TENANT_ID_KEY, tenantId);
    }

    public static UUID getRequired() {
        UUID tenantId = TENANT_ID_KEY.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantId;
    }
}
