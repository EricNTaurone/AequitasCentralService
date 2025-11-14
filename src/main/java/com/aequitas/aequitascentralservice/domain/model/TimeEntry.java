package com.aequitas.aequitascentralservice.domain.model;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root encapsulating the lifecycle of a tenant-scoped time entry.
 */
public final class TimeEntry {

    private final UUID id;
    private final UUID firmId;
    private final UUID userId;
    private final UUID customerId;
    private final UUID projectId;
    private final UUID matterId;
    private final String narrative;
    private final int durationMinutes;
    private final EntryStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final UUID approvedBy;
    private final Instant approvedAt;

    private TimeEntry(
            final UUID id,
            final UUID firmId,
            final UUID userId,
            final UUID customerId,
            final UUID projectId,
            final UUID matterId,
            final String narrative,
            final int durationMinutes,
            final EntryStatus status,
            final Instant createdAt,
            final Instant updatedAt,
            final UUID approvedBy,
            final Instant approvedAt) {
        this.id = id;
        this.firmId = firmId;
        this.userId = userId;
        this.customerId = customerId;
        this.projectId = projectId;
        this.matterId = matterId;
        this.narrative = narrative;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    /**
     * Rehydrates an aggregate from persistence.
     *
     * @param id identifier.
     * @param firmId firm identifier.
     * @param userId user identifier.
     * @param customerId customer identifier.
     * @param projectId project identifier.
     * @param matterId matter identifier.
     * @param narrative narrative.
     * @param durationMinutes duration.
     * @param status status.
     * @param createdAt created timestamp.
     * @param updatedAt updated timestamp.
     * @param approvedBy approver identifier.
     * @param approvedAt approval timestamp.
     * @return aggregate snapshot.
     */
    public static TimeEntry rehydrate(
            final UUID id,
            final UUID firmId,
            final UUID userId,
            final UUID customerId,
            final UUID projectId,
            final UUID matterId,
            final String narrative,
            final int durationMinutes,
            final EntryStatus status,
            final Instant createdAt,
            final Instant updatedAt,
            final UUID approvedBy,
            final Instant approvedAt) {
        return new TimeEntry(
                id,
                firmId,
                userId,
                customerId,
                projectId,
                matterId,
                narrative,
                durationMinutes,
                status,
                createdAt,
                updatedAt,
                approvedBy,
                approvedAt);
    }

    /**
     * Factory that builds a fresh draft entry.
     *
     * @param firmId tenant identifier.
     * @param userId owner identifier.
     * @param customerId linked customer identifier.
     * @param projectId linked project identifier.
     * @param matterId downstream matter identifier (nullable).
     * @param narrative time entry narrative.
     * @param durationMinutes duration in minutes.
     * @param now clock instant used for audit fields.
     * @return immutable draft aggregate.
     */
    public static TimeEntry draft(
            final UUID firmId,
            final UUID userId,
            final UUID customerId,
            final UUID projectId,
            final UUID matterId,
            final String narrative,
            final int durationMinutes,
            final Instant now) {
        requirePositiveDuration(durationMinutes);
        return new TimeEntry(
                UUID.randomUUID(),
                firmId,
                userId,
                customerId,
                projectId,
                matterId,
                requireNarrative(narrative),
                durationMinutes,
                EntryStatus.DRAFT,
                now,
                now,
                null,
                null);
    }

    private static void requirePositiveDuration(final int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (durationMinutes > 24 * 60) {
            throw new IllegalArgumentException("Duration exceeds 24 hours");
        }
    }

    private static String requireNarrative(final String narrative) {
        if (narrative == null || narrative.isBlank()) {
            throw new IllegalArgumentException("Narrative is required");
        }
        if (narrative.length() > 2048) {
            throw new IllegalArgumentException("Narrative exceeds 2048 characters");
        }
        return narrative;
    }

    /**
     * Creates a modified copy with new descriptive fields.
     *
     * @param newNarrative updated narrative.
     * @param newCustomerId updated customer id.
     * @param newProjectId updated project id.
     * @param newMatterId updated matter id.
     * @param newDuration updated duration.
     * @param now timestamp applied to {@code updatedAt}.
     * @return new immutable aggregate snapshot.
     */
    public TimeEntry updateDetails(
            final String newNarrative,
            final UUID newCustomerId,
            final UUID newProjectId,
            final UUID newMatterId,
            final int newDuration,
            final Instant now) {
        requirePositiveDuration(newDuration);
        return new TimeEntry(
                id,
                firmId,
                userId,
                newCustomerId,
                newProjectId,
                newMatterId,
                requireNarrative(newNarrative),
                newDuration,
                status,
                createdAt,
                now,
                approvedBy,
                approvedAt);
    }

    /**
     * Transitions the aggregate to SUBMITTED when it is still a draft.
     *
     * @param now timestamp applied to {@code updatedAt}.
     * @return a submitted snapshot.
     */
    public TimeEntry submit(final Instant now) {
        if (status != EntryStatus.DRAFT) {
            throw new IllegalStateException("Only draft entries can be submitted");
        }
        return new TimeEntry(
                id,
                firmId,
                userId,
                customerId,
                projectId,
                matterId,
                narrative,
                durationMinutes,
                EntryStatus.SUBMITTED,
                createdAt,
                now,
                approvedBy,
                approvedAt);
    }

    /**
     * Transitions the aggregate to APPROVED if it is currently submitted.
     *
     * @param approverId identifier performing the approval.
     * @param now timestamp applied to {@code approvedAt} and {@code updatedAt}.
     * @return approved snapshot.
     */
    public TimeEntry approve(final UUID approverId, final Instant now) {
        if (status != EntryStatus.SUBMITTED) {
            throw new IllegalStateException("Entry must be submitted before approval");
        }
        Objects.requireNonNull(approverId, "approverId");
        return new TimeEntry(
                id,
                firmId,
                userId,
                customerId,
                projectId,
                matterId,
                narrative,
                durationMinutes,
                EntryStatus.APPROVED,
                createdAt,
                now,
                approverId,
                now);
    }

    /**
     * @return aggregate identifier.
     */
    public UUID id() {
        return id;
    }

    /**
     * @return tenant identifier.
     */
    public UUID firmId() {
        return firmId;
    }

    /**
     * @return owner identifier.
     */
    public UUID userId() {
        return userId;
    }

    /**
     * @return associated customer identifier.
     */
    public UUID customerId() {
        return customerId;
    }

    /**
     * @return associated project identifier.
     */
    public UUID projectId() {
        return projectId;
    }

    /**
     * @return downstream matter identifier if present.
     */
    public UUID matterId() {
        return matterId;
    }

    /**
     * @return narrative text.
     */
    public String narrative() {
        return narrative;
    }

    /**
     * @return duration in minutes.
     */
    public int durationMinutes() {
        return durationMinutes;
    }

    /**
     * @return lifecycle status.
     */
    public EntryStatus status() {
        return status;
    }

    /**
     * @return creation timestamp.
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * @return last update timestamp.
     */
    public Instant updatedAt() {
        return updatedAt;
    }

    /**
     * @return approver identifier if approved.
     */
    public UUID approvedBy() {
        return approvedBy;
    }

    /**
     * @return approval timestamp if approved.
     */
    public Instant approvedAt() {
        return approvedAt;
    }
}
