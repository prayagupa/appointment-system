package com.zazzercode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zazzercode.config.TenantMetadataInterceptor;
import com.zazzercode.grpc.appointment.v1.AppointmentServiceGrpc;
import com.zazzercode.grpc.appointment.v1.GetProviderRequest;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
@SpringBootTest(
        properties = {
            "appointment.auth.enabled=true",
            "appointment.auth.api-keys=secret-key",
            "grpc.server.in-process-name=appointment-auth-test",
            "grpc.client.appointment.address=in-process:appointment-auth-test"
        })
@ActiveProfiles("test")
class AuthInterceptorTest {

  private static final UUID TENANT_HEALTH =
      UUID.fromString("11111111-1111-1111-1111-111111111111");

  @GrpcClient("appointment")
  private AppointmentServiceGrpc.AppointmentServiceBlockingStub stub;

  @Test
  void rejectsMissingAuthorization() {
    Metadata metadata = new Metadata();
    metadata.put(TenantMetadataInterceptor.TENANT_ID_KEY, TENANT_HEALTH.toString());
    assertThrows(
        StatusRuntimeException.class,
        () ->
            stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .getProvider(
                    GetProviderRequest.newBuilder()
                        .setProviderId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                        .build()));
  }

  @Test
  void acceptsValidApiKey() {
    Metadata metadata = new Metadata();
    metadata.put(TenantMetadataInterceptor.TENANT_ID_KEY, TENANT_HEALTH.toString());
    metadata.put(
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer secret-key");
    var provider =
        stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .withDeadlineAfter(5, TimeUnit.SECONDS)
            .getProvider(
                GetProviderRequest.newBuilder()
                    .setProviderId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                    .build());
    assertThat(provider.getDisplayName()).isEqualTo("Dr. Prayag");
  }
}
