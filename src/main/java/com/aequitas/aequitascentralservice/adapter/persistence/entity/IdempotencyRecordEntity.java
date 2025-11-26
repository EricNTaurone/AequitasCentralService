package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing stored idempotency executions.
 */
@Entity
@Table(name = IdempotencyRecordEntity.TABLE_NAME)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecordEntity {

    public static final String TABLE_NAME = "idempotency_records";
    public static final String OPERATION = "operation";
    public static final String USER_ID = "user_id";
    public static final String FIRM_ID = "firm_id";
    public static final String KEY_HASH = "key_hash";
    public static final String RESPONSE_ID = "response_id";
    public static final String CREATED_AT = "created_at";
    public static final String EXPIRES_AT = "expires_at";

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = OPERATION, nullable = false)
    private IdempotencyOperation operation;

    @Column(name = USER_ID, nullable = false)
    private UUID userId;

    @Column(name = FIRM_ID, nullable = false)
    private UUID firmId;

    @Column(name = KEY_HASH, nullable = false)
    private String keyHash;

    @Column(name = RESPONSE_ID, nullable = false)
    private UUID responseId;

    @Column(name = CREATED_AT, nullable = false)
    private Instant createdAt;

    @Column(name = EXPIRES_AT, nullable = false)
    private Instant expiresAt;

}
