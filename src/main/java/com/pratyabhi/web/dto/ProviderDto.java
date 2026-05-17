package com.pratyabhi.web.dto;

import com.pratyabhi.domain.Provider;
import java.util.UUID;

public record ProviderDto(UUID id, UUID tenantId, String displayName, String specialty) {

    public static ProviderDto from(Provider provider) {
        return new ProviderDto(
                provider.getId(),
                provider.getTenantId(),
                provider.getDisplayName(),
                provider.getSpecialty());
    }
}
