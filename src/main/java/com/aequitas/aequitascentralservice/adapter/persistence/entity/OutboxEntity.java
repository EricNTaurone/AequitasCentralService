package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

/**
 * JPA entity mapping the {@code outbox} table.
 */
@Entity
@Table(name = OutboxEntity.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OutboxEntity {

    public static final String TABLE_NAME = "outbox";
    public static final String FIRM_ID = "firm_id";
    public static final String AGGREGATE_ID = "aggregate_id";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_KEY = "event_key";
    public static final String PAYLOAD_JSON = "payload_json";
    public static final String OCCURRED_AT = "occurred_at";
    public static final String PUBLISHED_AT = "published_at";

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = FIRM_ID, nullable = false)
    private UUID firmId;

    @Column(name = AGGREGATE_ID, nullable = false)
    private UUID aggregateId;

    @Column(name = EVENT_TYPE, nullable = false)
    private String eventType;

    @Column(name = EVENT_KEY, nullable = false)
    private String eventKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = PAYLOAD_JSON, nullable = false, columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = OCCURRED_AT, nullable = false)
    private Instant occurredAt;

    @Column(name = PUBLISHED_AT)
    private Instant publishedAt;

}
