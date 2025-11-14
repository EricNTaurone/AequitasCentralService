package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

/**
 * JPA entity for the {@code time_entries} table.
 */
@Entity
@Table(name = "time_entries")
public class TimeEntryEntity {

    @Id
    private UUID id;

    @Column(name = "firm_id", nullable = false)
    private UUID firmId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "matter_id")
    private UUID matterId;

    @Column(name = "narrative", nullable = false, length = 2048)
    private String narrative;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntryStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getFirmId() {
        return firmId;
    }

    public void setFirmId(final UUID firmId) {
        this.firmId = firmId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getMatterId() {
        return matterId;
    }

    public void setMatterId(final UUID matterId) {
        this.matterId = matterId;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(final String narrative) {
        this.narrative = narrative;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(final int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public EntryStatus getStatus() {
        return status;
    }

    public void setStatus(final EntryStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(final Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(final UUID approvedBy) {
        this.approvedBy = approvedBy;
    }
}
