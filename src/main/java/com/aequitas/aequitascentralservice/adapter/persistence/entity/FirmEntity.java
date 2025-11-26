package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.aequitas.aequitascentralservice.adapter.persistence.embeddable.AddressEmbeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a firm in the persistence layer.
 */
@Entity
@Table(name = "firms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirmEntity {

    public static final String TABLE_NAME = "firms";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    @Id
    private UUID id;

    @Column(name = NAME, nullable = false, length = 255)
    private String name;

    @Embedded
    private AddressEmbeddable address;

    @Column(name = CREATED_AT, nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = UPDATED_AT, nullable = false)
    private Instant updatedAt;
}
