package com.pratyabhi.tenancy;

import io.grpc.Context;
import java.util.UUID;

public final class TenantContext {

    private static final Context.Key<UUID> TENANT_ID_KEY = Context.key("tenant-id");
    private static final ThreadLocal<UUID> THREAD_LOCAL = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Context attach(UUID tenantId) {
        THREAD_LOCAL.set(tenantId);
        return Context.current().withValue(TENANT_ID_KEY, tenantId);
    }

    public static void set(UUID tenantId) {
        THREAD_LOCAL.set(tenantId);
    }

    public static UUID getRequired() {
        UUID fromGrpc = TENANT_ID_KEY.get();
        if (fromGrpc != null) {
            return fromGrpc;
        }
        UUID fromThread = THREAD_LOCAL.get();
        if (fromThread != null) {
            return fromThread;
        }
        throw new IllegalStateException("Tenant context is not set");
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
