package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.OutboxPort;
import com.aequitas.aequitascentralservice.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JPA-backed implementation of the {@link OutboxPort}.
 */
@Component
public class OutboxRepositoryAdapter implements OutboxPort {

    private static final Logger log = LoggerFactory.getLogger(OutboxRepositoryAdapter.class);

    private final OutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxRepositoryAdapter(
            final OutboxJpaRepository repository, final ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(final UUID firmId, final UUID aggregateId, final DomainEvent event) {
        final OutboxEntity entity = new OutboxEntity();
        entity.setId(event.eventId());
        entity.setFirmId(firmId);
        entity.setAggregateId(aggregateId);
        entity.setEventType(event.eventType());
        entity.setEventKey(aggregateId + "::" + event.eventType());
        entity.setPayloadJson(toJson(event));
        entity.setOccurredAt(event.occurredAt());
        repository.save(entity);
    }

    private String toJson(final DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}", event.eventType(), e);
            throw new IllegalStateException("Unable to serialize event payload", e);
        }
    }
}
