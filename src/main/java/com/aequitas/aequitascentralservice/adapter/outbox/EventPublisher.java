package com.aequitas.aequitascentralservice.adapter.outbox;

/**
 * Publishes serialized events to an external messaging system.
 */
public interface EventPublisher {

    /**
     * Publishes the serialized payload to the downstream bus.
     *
     * @param eventType canonical event type.
     * @param payload serialized payload.
     * @param partitionKey partition key used for ordering.
     * @param deduplicationKey dedupe key guarding against duplicates.
     */
    void publish(String eventType, String payload, String partitionKey, String deduplicationKey);
}
