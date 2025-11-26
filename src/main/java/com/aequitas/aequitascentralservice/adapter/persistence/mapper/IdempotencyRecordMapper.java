package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.IdempotencyRecordEntity;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;

import lombok.experimental.UtilityClass;

/**
 * Mapper bridging idempotency persistence entities and domain records.
 */
@UtilityClass
public final class IdempotencyRecordMapper {

    /**
     * Converts to domain.
     *
     * @param entity entity snapshot.
     * @return domain record.
     */
    public static IdempotencyRecord toDomain(final IdempotencyRecordEntity entity) {
        return IdempotencyRecord.builder()
                .id(entity.getId())
                .operation(entity.getOperation())
                .userId(entity.getUserId())
                .firmId(entity.getFirmId())
                .keyHash(entity.getKeyHash())
                .responseId(entity.getResponseId())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /**
     * Converts to entity.
     *
     * @param record domain record.
     * @return entity snapshot.
     */
    public static IdempotencyRecordEntity toEntity(final IdempotencyRecord record) {
        return IdempotencyRecordEntity.builder()
                .id(record.id())
                .operation(record.operation())
                .userId(record.userId())
                .firmId(record.firmId())
                .keyHash(record.keyHash())
                .responseId(record.responseId())
                .createdAt(record.createdAt())
                .expiresAt(record.expiresAt())
                .build();
    }
}
