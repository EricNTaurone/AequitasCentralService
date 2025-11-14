package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.mapper.IdempotencyRecordMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.IdempotencyJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.IdempotencyRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adapter persisting idempotency records through Spring Data JPA.
 */
@Component
public class IdempotencyRepositoryAdapter implements IdempotencyRepositoryPort {

    private final IdempotencyJpaRepository repository;

    public IdempotencyRepositoryAdapter(final IdempotencyJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<IdempotencyRecord> find(
            final IdempotencyOperation operation, final UUID userId, final String keyHash) {
        return repository
                .findByOperationAndUserIdAndKeyHash(operation, userId, keyHash)
                .map(IdempotencyRecordMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdempotencyRecord save(final IdempotencyRecord record) {
        return IdempotencyRecordMapper.toDomain(
                repository.save(IdempotencyRecordMapper.toEntity(record)));
    }
}
