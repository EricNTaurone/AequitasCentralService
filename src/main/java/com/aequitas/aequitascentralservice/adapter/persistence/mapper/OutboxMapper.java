package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.domain.model.OutboxMessage;

/**
 * Mapper bridging outbox domain and persistence representations.
 */
public final class OutboxMapper {

    private OutboxMapper() {
    }

    /**
     * @param message domain model.
     * @return entity representation.
     */
    public static OutboxEntity toEntity(final OutboxMessage message) {
        final OutboxEntity entity = OutboxEntity.builder()
                .id(message.id())
                .firmId(message.firmId())
                .aggregateId(message.aggregateId())
                .eventType(message.eventType())
                .payloadJson(message.payloadJson())
                .occurredAt(message.occurredAt())
                .publishedAt(message.publishedAt())
                .eventKey(message.aggregateId().toString() + "-" + message.eventType())
                .build();
        return entity;
    }

    /**
     * @param entity entity snapshot.
     * @return domain outbox row.
     */
    public static OutboxMessage toDomain(final OutboxEntity entity) {
        return new OutboxMessage(
                entity.getId(),
                entity.getFirmId(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayloadJson(),
                entity.getOccurredAt(),
                entity.getPublishedAt());
    }
}
