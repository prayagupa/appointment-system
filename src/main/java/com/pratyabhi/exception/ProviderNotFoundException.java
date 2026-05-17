package com.pratyabhi.exception;

import java.util.UUID;

public class ProviderNotFoundException extends DomainException {

    public ProviderNotFoundException(UUID providerId) {
        super("PROVIDER_NOT_FOUND", "Provider not found: " + providerId);
    }
}
