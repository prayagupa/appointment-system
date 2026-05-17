package com.pratyabhi.grpc;

import com.google.protobuf.Empty;
import com.pratyabhi.grpc.appointment.v1.AppointmentServiceGrpc;
import com.pratyabhi.grpc.appointment.v1.BookAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.BookAppointmentResponse;
import com.pratyabhi.grpc.appointment.v1.CancelAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.GetAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.GetProviderRequest;
import com.pratyabhi.grpc.appointment.v1.ListAvailabilityRequest;
import com.pratyabhi.grpc.appointment.v1.ListAvailabilityResponse;
import com.pratyabhi.service.AppointmentService;
import com.pratyabhi.service.AvailabilityService;
import com.pratyabhi.service.ProviderService;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AppointmentGrpcService extends AppointmentServiceGrpc.AppointmentServiceImplBase {

    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    public AppointmentGrpcService(
            ProviderService providerService,
            AppointmentService appointmentService,
            AvailabilityService availabilityService) {
        this.providerService = providerService;
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    @Override
    public void getProvider(GetProviderRequest request, StreamObserver<com.pratyabhi.grpc.appointment.v1.Provider> response) {
        var provider = providerService.get(UUID.fromString(request.getProviderId()));
        response.onNext(ProtoMapper.toProvider(provider));
        response.onCompleted();
    }

    @Override
    public void getAppointment(
            GetAppointmentRequest request,
            StreamObserver<com.pratyabhi.grpc.appointment.v1.Appointment> response) {
        var appointment = appointmentService.findById(UUID.fromString(request.getAppointmentId()));
        response.onNext(ProtoMapper.toAppointment(appointment));
        response.onCompleted();
    }

    @Override
    public void bookAppointment(BookAppointmentRequest request, StreamObserver<BookAppointmentResponse> response) {
        var appointment = appointmentService.book(
                UUID.fromString(request.getProviderId()),
                request.getPatientRef(),
                ProtoMapper.toInstant(request.getStartTime()),
                request.getDurationMinutes());
        response.onNext(BookAppointmentResponse.newBuilder()
                .setAppointmentId(appointment.getId().toString())
                .setStatus(appointment.getStatus().name())
                .build());
        response.onCompleted();
    }

    @Override
    public void cancelAppointment(CancelAppointmentRequest request, StreamObserver<Empty> response) {
        appointmentService.cancel(UUID.fromString(request.getAppointmentId()));
        response.onNext(Empty.getDefaultInstance());
        response.onCompleted();
    }

    @Override
    public void listAvailability(
            ListAvailabilityRequest request, StreamObserver<ListAvailabilityResponse> response) {
        int slotDuration = request.getSlotDurationMinutes() == 0 ? 30 : request.getSlotDurationMinutes();
        var slots = availabilityService.listAvailability(
                UUID.fromString(request.getProviderId()),
                ProtoMapper.toInstant(request.getRangeStart()),
                ProtoMapper.toInstant(request.getRangeEnd()),
                slotDuration);
        var builder = ListAvailabilityResponse.newBuilder();
        slots.forEach(slot -> builder.addSlots(ProtoMapper.toTimeSlot(slot)));
        response.onNext(builder.build());
        response.onCompleted();
    }
}
