package com.zazzercode;

import static org.assertj.core.api.Assertions.assertThat;

import com.zazzercode.repository.ProviderRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProviderRepositoryTest {

  @Autowired private ProviderRepository providerRepository;

  @Test
  void seedProviderExists() {
    var provider =
        providerRepository.findByIdAndTenantId(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            UUID.fromString("11111111-1111-1111-1111-111111111111"));
    assertThat(provider).isPresent();
  }
}
