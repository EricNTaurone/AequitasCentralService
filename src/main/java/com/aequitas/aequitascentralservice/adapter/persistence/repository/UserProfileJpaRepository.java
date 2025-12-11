package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.UserProfileEntity;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link UserProfileEntity}.
 */
public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {

    Optional<UserProfileEntity> findByIdAndFirmId(UUID id, UUID firmId);

    Optional<UserProfileEntity> findByAuthenticationId(UUID authenticationId);

    List<UserProfileEntity> findByFirmId(UUID firmId);

    List<UserProfileEntity> findByFirmIdAndRole(UUID firmId, Role role);
}
