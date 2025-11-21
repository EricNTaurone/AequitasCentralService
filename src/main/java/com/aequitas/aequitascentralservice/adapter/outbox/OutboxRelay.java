package com.aequitas.aequitascentralservice.adapter.outbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;

import lombok.extern.slf4j.Slf4j;

/**
 * Polls the outbox table and relays unpublished events to the configured {@link EventPublisher}.
 */
@Component
@Slf4j
public class OutboxRelay {

    private final OutboxJpaRepository outboxJpaRepository;
    private final EventPublisher eventPublisher;
    private final ClockPort clockPort;

    public OutboxRelay(
            final OutboxJpaRepository outboxJpaRepository,
            final EventPublisher eventPublisher,
            final ClockPort clockPort) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.eventPublisher = eventPublisher;
        this.clockPort = clockPort;
    }

    /**
     * Executes every few seconds to push events to the transport.
     */
    @Scheduled(fixedDelayString = "${outbox.relay-interval:PT5S}")
    @Transactional
    public void relay() {
        final List<OutboxEntity> batch =
                outboxJpaRepository.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
        if (batch.isEmpty()) {
            return;
        }
        log.debug("Relaying {} outbox events", batch.size());
        batch.forEach(
                entity -> {
                    eventPublisher.publish(
                            entity.getEventType(),
                            entity.getPayloadJson(),
                            entity.getFirmId().toString(),
                            entity.getEventKey());
                    entity.setPublishedAt(clockPort.now());
                });
        outboxJpaRepository.saveAll(batch);
    }
}
