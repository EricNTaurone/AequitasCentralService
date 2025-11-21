package com.aequitas.aequitascentralservice.adapter.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggingEventPublisherTest {

    private static final String EVENT_TYPE = "testEvent";
    private static final String PAYLOAD = "{\"key\":\"value\"}";
    private static final String PARTITION_KEY = "partition1";
    private static final String DEDUPLICATION_KEY = "dedupe1";

    private LoggingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new LoggingEventPublisher();
    }

    @Test
    void GIVEN_validInput_WHEN_publish_THEN_logsInfo() {
        // Given/When
        publisher.publish(EVENT_TYPE, PAYLOAD, PARTITION_KEY, DEDUPLICATION_KEY);
    }
}
