package com.pratyabhi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.Timestamp;
import com.pratyabhi.config.TenantMetadataInterceptor;
import com.pratyabhi.grpc.appointment.v1.AppointmentServiceGrpc;
import com.pratyabhi.grpc.appointment.v1.BookAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.CancelAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.GetAppointmentRequest;
import com.pratyabhi.grpc.appointment.v1.GetProviderRequest;
import com.pratyabhi.grpc.appointment.v1.ListAvailabilityRequest;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentRpcIntegrationTest {

  private static final UUID TENANT_HEALTH =
      UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PROVIDER_HEALTH =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID TENANT_SALON =
      UUID.fromString("22222222-2222-2222-2222-222222222222");

  @GrpcClient("appointment")
  private AppointmentServiceGrpc.AppointmentServiceBlockingStub stub;

  @Test
  void getProvider_returnsSeededProvider() {
    var provider =
        tenantStub(TENANT_HEALTH)
            .getProvider(
                GetProviderRequest.newBuilder()
                    .setProviderId(PROVIDER_HEALTH.toString())
                    .build());
    assertThat(provider.getDisplayName()).isEqualTo("Dr. Prayag");
    assertThat(provider.getTenantId()).isEqualTo(TENANT_HEALTH.toString());
  }

  @Test
  void getProvider_unknownProvider_notFound() {
    assertThrows(
        StatusRuntimeException.class,
        () ->
            tenantStub(TENANT_HEALTH)
                .getProvider(
                    GetProviderRequest.newBuilder()
                        .setProviderId(UUID.randomUUID().toString())
                        .build()));
  }

  @Test
  void bookAndCancelAppointment_happyPath() {
    Instant start = nextMondayAt(10, 0);
    var booked =
        tenantStub(TENANT_HEALTH)
            .bookAppointment(
                BookAppointmentRequest.newBuilder()
                    .setProviderId(PROVIDER_HEALTH.toString())
                    .setPatientRef("patient-001")
                    .setStartTime(toTimestamp(start))
                    .setDurationMinutes(30)
                    .build());
    assertThat(booked.getStatus()).isEqualTo("BOOKED");

    var fetched =
        tenantStub(TENANT_HEALTH)
            .getAppointment(
                GetAppointmentRequest.newBuilder()
                    .setAppointmentId(booked.getAppointmentId())
                    .build());
    assertThat(fetched.getPatientRef()).isEqualTo("patient-001");

    tenantStub(TENANT_HEALTH)
        .cancelAppointment(
            CancelAppointmentRequest.newBuilder()
                .setAppointmentId(booked.getAppointmentId())
                .build());

    var cancelled =
        tenantStub(TENANT_HEALTH)
            .getAppointment(
                GetAppointmentRequest.newBuilder()
                    .setAppointmentId(booked.getAppointmentId())
                    .build());
    assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
  }

  @Test
  void doubleBooking_sameSlot_fails() {
    Instant start = nextMondayAt(11, 0);
    tenantStub(TENANT_HEALTH)
        .bookAppointment(
            BookAppointmentRequest.newBuilder()
                .setProviderId(PROVIDER_HEALTH.toString())
                .setPatientRef("patient-a")
                .setStartTime(toTimestamp(start))
                .setDurationMinutes(30)
                .build());

    assertThrows(
        StatusRuntimeException.class,
        () ->
            tenantStub(TENANT_HEALTH)
                .bookAppointment(
                    BookAppointmentRequest.newBuilder()
                        .setProviderId(PROVIDER_HEALTH.toString())
                        .setPatientRef("patient-b")
                        .setStartTime(toTimestamp(start))
                        .setDurationMinutes(30)
                        .build()));
  }

  @Test
  void tenantIsolation_otherTenantCannotSeeProvider() {
    assertThrows(
        StatusRuntimeException.class,
        () ->
            tenantStub(TENANT_SALON)
                .getProvider(
                    GetProviderRequest.newBuilder()
                        .setProviderId(PROVIDER_HEALTH.toString())
                        .build()));
  }

  @Test
  void listAvailability_returnsOpenSlots() {
    Instant rangeStart = nextMondayAt(0, 0);
    Instant rangeEnd = rangeStart.plus(1, ChronoUnit.DAYS);
    var response =
        tenantStub(TENANT_HEALTH)
            .listAvailability(
                ListAvailabilityRequest.newBuilder()
                    .setProviderId(PROVIDER_HEALTH.toString())
                    .setRangeStart(toTimestamp(rangeStart))
                    .setRangeEnd(toTimestamp(rangeEnd))
                    .setSlotDurationMinutes(60)
                    .build());
    assertThat(response.getSlotsCount()).isGreaterThan(0);
  }

  @Test
  void missingTenantMetadata_invalidArgument() {
    assertThrows(
        StatusRuntimeException.class,
        () ->
            stub.withDeadlineAfter(5, TimeUnit.SECONDS)
                .getProvider(
                    GetProviderRequest.newBuilder()
                        .setProviderId(PROVIDER_HEALTH.toString())
                        .build()));
  }

  private AppointmentServiceGrpc.AppointmentServiceBlockingStub tenantStub(UUID tenantId) {
    Metadata metadata = new Metadata();
    metadata.put(TenantMetadataInterceptor.TENANT_ID_KEY, tenantId.toString());
    return stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
        .withDeadlineAfter(10, TimeUnit.SECONDS);
  }

  private static Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  private static Instant nextMondayAt(int hour, int minute) {
    LocalDate date = LocalDate.now(ZoneOffset.UTC);
    int daysUntilMonday =
        (java.time.DayOfWeek.MONDAY.getValue() - date.getDayOfWeek().getValue() + 7) % 7;
    if (daysUntilMonday == 0) {
      daysUntilMonday = 7;
    }
    LocalDate monday = date.plusDays(daysUntilMonday);
    return monday.atTime(hour, minute).toInstant(ZoneOffset.UTC);
  }
}
