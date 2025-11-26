package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA mapping for the {@code projects} table.
 */
@Entity
@Table(name = ProjectEntity.TABLE_NAME)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
    public static final String TABLE_NAME = "projects";
    public static final String FIRM_ID = "firm_id";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "created_at";

    @Id
    private UUID id;

    @Column(name = FIRM_ID, nullable = false)
    private UUID firmId;

    @Column(name = CUSTOMER_ID, nullable = false)
    private UUID customerId;

    @Column(name = NAME, nullable = false)
    private String name;

    @Column(name = STATUS, nullable = false)
    private String status;

    @Column(name = CREATED_AT, nullable = false)
    private Instant createdAt;
}
