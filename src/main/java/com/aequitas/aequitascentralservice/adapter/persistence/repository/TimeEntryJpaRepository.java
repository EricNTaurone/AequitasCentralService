package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data repository for {@link TimeEntryEntity}.
 */
public interface TimeEntryJpaRepository
        extends JpaRepository<TimeEntryEntity, UUID>, JpaSpecificationExecutor<TimeEntryEntity> {

    /**
     * @param id entry identifier.
     * @param firmId tenant identifier.
     * @return optional entity scoped to the firm.
     */
    Optional<TimeEntryEntity> findByIdAndFirmId(UUID id, UUID firmId);
}
