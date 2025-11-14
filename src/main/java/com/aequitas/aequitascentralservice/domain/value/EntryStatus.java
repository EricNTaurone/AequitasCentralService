package com.aequitas.aequitascentralservice.domain.value;

/**
 * Represents the lifecycle phases for a time entry aggregate.
 */
public enum EntryStatus {
    /**
     * Draft entries are editable and not yet submitted for approval.
     */
    DRAFT,
    /**
     * Submitted entries await manager review.
     */
    SUBMITTED,
    /**
     * Approved entries are immutable and ready for export.
     */
    APPROVED
}
