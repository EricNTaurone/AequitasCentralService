package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import java.util.UUID;

/**
 * Inbound port describing command-side operations for the time entry aggregate.
 */
public interface TimeEntryCommandPort {

    /**
     * Creates a draft entry owned by the authenticated user.
     *
     * @param command user supplied attributes.
     * @return identifier for the newly created entry.
     */
    UUID create(CreateTimeEntryCommand command);

    /**
     * Applies partial updates to an existing draft/submitted entry.
     *
     * @param id entry identifier.
     * @param command patch document.
     */
    void update(UUID id, UpdateTimeEntryCommand command);

    /**
     * Submits the entry for approval.
     *
     * @param id entry identifier.
     */
    void submit(UUID id);

    /**
     * Approves the entry, enforcing RBAC rules.
     *
     * @param id entry identifier.
     */
    void approve(UUID id);
}
