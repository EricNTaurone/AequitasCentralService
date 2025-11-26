package com.aequitas.aequitascentralservice.app.port.inbound;

import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;

/**
 * Inbound port for command-side firm operations.
 */
public interface FirmCommandPort {

    /**
     * Creates a new firm with the provided details.
     *
     * @param command creation command containing firm details.
     * @return identifier of the newly created firm.
     */
    UUID create(CreateFirmCommand command);

    /**
     * Updates an existing firm's mutable attributes.
     *
     * @param id firm identifier.
     * @param command update command containing new values.
     */
    void update(UUID id, UpdateFirmCommand command);
}
