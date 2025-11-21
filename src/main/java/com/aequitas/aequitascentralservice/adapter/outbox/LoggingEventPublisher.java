package com.aequitas.aequitascentralservice.adapter.outbox;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple {@link EventPublisher} that logs payloads for local development.
 */
@Component
@Slf4j
public class LoggingEventPublisher implements EventPublisher {

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(
            final String eventType,
            final String payload,
            final String partitionKey,
            final String deduplicationKey) {
        log.info(
                "Publishing eventType={} partitionKey={} dedupeKey={} payload={}",
                eventType,
                partitionKey,
                deduplicationKey,
                payload);
    }
}
