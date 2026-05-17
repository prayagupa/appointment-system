package com.pratyabhi.web.dto;

import com.pratyabhi.domain.Tenant;
import java.util.UUID;

public record TenantDto(UUID id, String name, String type) {

    public static TenantDto from(Tenant tenant) {
        return new TenantDto(tenant.getId(), tenant.getName(), tenant.getType().name());
    }
}
