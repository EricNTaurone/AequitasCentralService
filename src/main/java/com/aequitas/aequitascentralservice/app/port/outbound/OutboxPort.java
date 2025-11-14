package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.event.DomainEvent;
import java.util.UUID;

/**
 * Outbound port responsible for persisting domain events alongside aggregate changes.
 */
public interface OutboxPort {

    /**
     * Persists the supplied domain event for later relay.
     *
     * @param firmId tenant identifier used for FIFO ordering.
     * @param aggregateId aggregate identifier.
     * @param event domain event payload.
     */
    void append(UUID firmId, UUID aggregateId, DomainEvent event);
}
