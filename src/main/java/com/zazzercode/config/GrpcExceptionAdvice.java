package com.zazzercode.config;

import com.zazzercode.exception.AppointmentNotFoundException;
import com.zazzercode.exception.InvalidArgumentException;
import com.zazzercode.exception.ProviderNotFoundException;
import com.zazzercode.exception.SlotUnavailableException;
import com.zazzercode.exception.TenantNotFoundException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcAdvice
public class GrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    @GrpcExceptionHandler(TenantNotFoundException.class)
    public Status handleTenantNotFound(TenantNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(ProviderNotFoundException.class)
    public Status handleProviderNotFound(ProviderNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(AppointmentNotFoundException.class)
    public Status handleAppointmentNotFound(AppointmentNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(SlotUnavailableException.class)
    public Status handleSlotUnavailable(SlotUnavailableException ex) {
        return Status.FAILED_PRECONDITION.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(InvalidArgumentException.class)
    public Status handleInvalidArgument(InvalidArgumentException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public Status handleIllegalArgument(IllegalArgumentException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleUnexpected(Exception ex) {
        log.error("Unhandled gRPC exception", ex);
        return Status.INTERNAL.withDescription(ex.getMessage());
    }
}
