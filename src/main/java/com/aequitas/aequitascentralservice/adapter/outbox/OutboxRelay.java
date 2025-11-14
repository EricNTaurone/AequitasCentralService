package com.aequitas.aequitascentralservice.adapter.outbox;

import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the outbox table and relays unpublished events to the configured {@link EventPublisher}.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

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
        final List<com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity> batch =
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
