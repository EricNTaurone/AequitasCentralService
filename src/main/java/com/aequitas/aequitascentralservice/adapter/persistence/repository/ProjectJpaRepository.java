package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.ProjectEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link ProjectEntity}.
 */
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {

    /**
     * @param id project identifier.
     * @param firmId tenant identifier.
     * @return optional entity.
     */
    Optional<ProjectEntity> findByIdAndFirmId(UUID id, UUID firmId);
}
