package com.aequitas.aequitascentralservice.adapter.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.OutboxPort;
import com.aequitas.aequitascentralservice.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * JPA-backed implementation of the {@link OutboxPort}.
 */
@Component
@Slf4j
public class OutboxRepositoryAdapter implements OutboxPort {

    private final OutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxRepositoryAdapter(
            final OutboxJpaRepository repository, final ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(final UUID firmId, final UUID aggregateId, final DomainEvent event) {
        final OutboxEntity entity = OutboxEntity.builder()
                .id(event.eventId())
                .firmId(firmId)
                .aggregateId(aggregateId)
                .eventType(event.eventType())
                .eventKey(aggregateId + "::" + event.eventType())
                .payloadJson(toJson(event))
                .occurredAt(event.occurredAt())
                .build();

        repository.save(entity);
    }

    private String toJson(final DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize event payload", e);
        }
    }
}
