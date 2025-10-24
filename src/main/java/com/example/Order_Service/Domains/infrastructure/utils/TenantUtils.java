package com.example.Order_Service.Domains.infrastructure.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantUtils {

    public String extractTenantId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        // Try to read tenant_id first, then fallback to ORDER_TENANT
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId == null) {
            tenantId = jwt.getClaimAsString("ORDER_TENANT");
        }

        return tenantId;
    }
}
