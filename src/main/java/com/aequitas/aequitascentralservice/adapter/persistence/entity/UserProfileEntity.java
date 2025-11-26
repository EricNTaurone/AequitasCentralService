package com.aequitas.aequitascentralservice.adapter.persistence.entity;

import com.aequitas.aequitascentralservice.domain.value.Role;
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

import java.util.UUID;

/**
 * JPA entity mapping the {@code user_profiles} table.
 */
@Entity
@Table(name = UserProfileEntity.TABLE_NAME)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {
    public static final String TABLE_NAME = "user_profiles";
    public static final String FIRM_ID = "firm_id";
    public static final String EMAIL = "email";
    public static final String ROLE = "role";

    @Id
    private UUID id;

    @Column(name = FIRM_ID, nullable = false)
    private UUID firmId;

    @Column(name = EMAIL, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = ROLE, nullable = false)
    private Role role;
}
