package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.domain.model.OutboxMessage;

import lombok.experimental.UtilityClass;

/**
 * Mapper bridging outbox domain and persistence representations.
 */
@UtilityClass
public final class OutboxMapper {

    /**
     * @param message domain model.
     * @return entity representation.
     */
    public static OutboxEntity toEntity(final OutboxMessage message) {
        return OutboxEntity.builder()
                .id(message.id())
                .firmId(message.firmId())
                .aggregateId(message.aggregateId())
                .eventType(message.eventType())
                .payloadJson(message.payloadJson())
                .occurredAt(message.occurredAt())
                .publishedAt(message.publishedAt())
                .eventKey(message.aggregateId().toString() + "-" + message.eventType())
                .build();
    }

    /**
     * @param entity entity snapshot.
     * @return domain outbox row.
     */
    public static OutboxMessage toDomain(final OutboxEntity entity) {
        return OutboxMessage.builder()
                .id(entity.getId())
                .firmId(entity.getFirmId())
                .aggregateId(entity.getAggregateId())
                .eventType(entity.getEventType())
                .payloadJson(entity.getPayloadJson())
                .occurredAt(entity.getOccurredAt())
                .publishedAt(entity.getPublishedAt())
                .build();
    }
}
