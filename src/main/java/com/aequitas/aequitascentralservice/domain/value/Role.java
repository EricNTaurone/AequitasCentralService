package com.aequitas.aequitascentralservice.domain.value;

/**
 * Enumerates the supported RBAC roles within a firm.
 */
public enum Role {
    /**
     * Individual contributor with access to own entries only.
     */
    EMPLOYEE,
    /**
     * People lead who may manage subordinate time entries.
     */
    MANAGER,
    /**
     * Firm administrator with full access inside the tenant.
     */
    ADMIN,
    
    /**
     * System administrator with access across all tenants.
     */
    SUPER_ADMIN
}
