package com.zazzercode;

import static org.assertj.core.api.Assertions.assertThat;

import com.zazzercode.service.ProviderService;
import com.zazzercode.tenancy.TenantContext;
import io.grpc.Context;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProviderServiceTest {

  @Autowired private ProviderService providerService;

  @Test
  void getProvider_withGrpcContext() {
    UUID tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID providerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    Context context = TenantContext.attach(tenantId);
  context.wrap(
          () -> {
            var provider = providerService.get(providerId);
            assertThat(provider.getDisplayName()).isEqualTo("Dr. Prayag");
          })
        .run();
  }
}
