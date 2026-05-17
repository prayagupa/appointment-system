package com.zazzercode.config;

import com.zazzercode.tenancy.TenantContext;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.UUID;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

@GrpcGlobalServerInterceptor
@Order(1)
public class TenantMetadataInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TENANT_ID_KEY =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String tenantHeader = headers.get(TENANT_ID_KEY);
        if (tenantHeader == null || tenantHeader.isBlank()) {
            call.close(
                    Status.INVALID_ARGUMENT.withDescription("Missing required metadata: x-tenant-id"),
                    new Metadata());
            return new ServerCall.Listener<>() {};
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantHeader.trim());
        } catch (IllegalArgumentException ex) {
            call.close(
                    Status.INVALID_ARGUMENT.withDescription("Invalid x-tenant-id: must be a UUID"),
                    new Metadata());
            return new ServerCall.Listener<>() {};
        }

        Context context = TenantContext.attach(tenantId);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
