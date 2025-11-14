package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence abstraction for idempotency keys.
 */
public interface IdempotencyRepositoryPort {

    /**
     * Attempts to load an existing key for the combination of user and operation.
     *
     * @param operation logical operation.
     * @param userId user identifier.
     * @param keyHash hashed representation of the client key.
     * @return optional record.
     */
    Optional<IdempotencyRecord> find(IdempotencyOperation operation, UUID userId, String keyHash);

    /**
     * Persists the supplied record.
     *
     * @param record record to save.
     * @return saved record.
     */
    IdempotencyRecord save(IdempotencyRecord record);
}
