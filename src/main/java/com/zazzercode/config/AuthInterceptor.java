package com.zazzercode.config;

import com.zazzercode.exception.UnauthorizedException;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@GrpcGlobalServerInterceptor
@Order(0)
public class AuthInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final boolean enabled;
    private final Set<String> apiKeys;

    public AuthInterceptor(
            @Value("${appointment.auth.enabled:false}") boolean enabled,
            @Value("${appointment.auth.api-keys:}") String apiKeys) {
        this.enabled = enabled;
        this.apiKeys = Arrays.stream(apiKeys.split(","))
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        if (!enabled) {
            return next.startCall(call, headers);
        }

        String authorization = headers.get(AUTHORIZATION_KEY);
        if (authorization == null || authorization.isBlank()) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing authorization"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization.trim();
        if (!apiKeys.contains(token)) {
            call.close(Status.PERMISSION_DENIED.withDescription("Invalid API key"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        return next.startCall(call, headers);
    }
}
