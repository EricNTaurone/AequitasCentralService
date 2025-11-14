package com.aequitas.aequitascentralservice.domain.value;

/**
 * Enumerates the supported idempotent workflows enforced by the service.
 */
public enum IdempotencyOperation {
    /**
     * Draft creation command.
     */
    TIME_ENTRY_CREATE,
    /**
     * Approval command.
     */
    TIME_ENTRY_APPROVE
}
