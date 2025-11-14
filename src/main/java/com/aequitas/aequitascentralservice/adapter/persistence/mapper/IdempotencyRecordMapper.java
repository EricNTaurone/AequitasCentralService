package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.IdempotencyRecordEntity;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;

/**
 * Mapper bridging idempotency persistence entities and domain records.
 */
public final class IdempotencyRecordMapper {

    private IdempotencyRecordMapper() {}

    /**
     * Converts to domain.
     *
     * @param entity entity snapshot.
     * @return domain record.
     */
    public static IdempotencyRecord toDomain(final IdempotencyRecordEntity entity) {
        return new IdempotencyRecord(
                entity.getId(),
                entity.getOperation(),
                entity.getUserId(),
                entity.getFirmId(),
                entity.getKeyHash(),
                entity.getResponseId(),
                entity.getCreatedAt(),
                entity.getExpiresAt());
    }

    /**
     * Converts to entity.
     *
     * @param record domain record.
     * @return entity snapshot.
     */
    public static IdempotencyRecordEntity toEntity(final IdempotencyRecord record) {
        final IdempotencyRecordEntity entity = new IdempotencyRecordEntity();
        entity.setId(record.id());
        entity.setOperation(record.operation());
        entity.setUserId(record.userId());
        entity.setFirmId(record.firmId());
        entity.setKeyHash(record.keyHash());
        entity.setResponseId(record.responseId());
        entity.setCreatedAt(record.createdAt());
        entity.setExpiresAt(record.expiresAt());
        return entity;
    }
}
