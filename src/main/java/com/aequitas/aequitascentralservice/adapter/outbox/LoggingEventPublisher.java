package com.aequitas.aequitascentralservice.adapter.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple {@link EventPublisher} that logs payloads for local development.
 */
@Component
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

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
                "Publishing eventType={} partitionKey={} dedupKey={} payload={}",
                eventType,
                partitionKey,
                deduplicationKey,
                payload);
    }
}
