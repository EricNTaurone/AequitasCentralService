package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.IdempotencyRecordEntity;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link IdempotencyRecordEntity}.
 */
public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyRecordEntity, UUID> {

    /**
     * @param operation operation identifier.
     * @param userId user identifier.
     * @param keyHash hashed key.
     * @return optional persisted record.
     */
    Optional<IdempotencyRecordEntity> findByOperationAndUserIdAndKeyHash(
            IdempotencyOperation operation, UUID userId, String keyHash);
}
