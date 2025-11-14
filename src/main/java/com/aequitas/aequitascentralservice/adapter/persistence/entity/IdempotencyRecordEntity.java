package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing stored idempotency executions.
 */
@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecordEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private IdempotencyOperation operation;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "firm_id", nullable = false)
    private UUID firmId;

    @Column(name = "key_hash", nullable = false)
    private String keyHash;

    @Column(name = "response_id", nullable = false)
    private UUID responseId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public IdempotencyOperation getOperation() {
        return operation;
    }

    public void setOperation(final IdempotencyOperation operation) {
        this.operation = operation;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public UUID getFirmId() {
        return firmId;
    }

    public void setFirmId(final UUID firmId) {
        this.firmId = firmId;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(final String keyHash) {
        this.keyHash = keyHash;
    }

    public UUID getResponseId() {
        return responseId;
    }

    public void setResponseId(final UUID responseId) {
        this.responseId = responseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
