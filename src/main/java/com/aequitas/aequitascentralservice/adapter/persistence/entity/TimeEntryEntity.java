package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

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

/**
 * JPA entity for the {@code time_entries} table.
 */
@Entity
@Table(name = TimeEntryEntity.TABLE_NAME)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryEntity {
    public static final String TABLE_NAME = "time_entries";
    public static final String FIRM_ID = "firm_id";
    public static final String USER_ID = "user_id";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String PROJECT_ID = "project_id";
    public static final String MATTER_ID = "matter_id";
    public static final String NARRATIVE = "narrative";
    public static final String DURATION_MINUTES = "duration_minutes";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String APPROVED_AT = "approved_at";
    public static final String APPROVED_BY = "approved_by";

    @Id
    private UUID id;

    @Column(name = FIRM_ID, nullable = false)
    private UUID firmId;

    @Column(name = USER_ID, nullable = false)
    private UUID userId;

    @Column(name = CUSTOMER_ID, nullable = false)
    private UUID customerId;

    @Column(name = PROJECT_ID, nullable = false)
    private UUID projectId;

    @Column(name = MATTER_ID)
    private UUID matterId;

    @Column(name = NARRATIVE, nullable = false, length = 2048)
    private String narrative;

    @Column(name = DURATION_MINUTES, nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS, nullable = false)
    private EntryStatus status;

    @Column(name = CREATED_AT, nullable = false)
    private Instant createdAt;

    @Column(name = UPDATED_AT, nullable = false)
    private Instant updatedAt;

    @Column(name = APPROVED_AT)
    private Instant approvedAt;

    @Column(name = APPROVED_BY)
    private UUID approvedBy;
}
