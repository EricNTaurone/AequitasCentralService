package com.aequitas.aequitascentralservice.app.port.outbound;

import java.time.Instant;

/**
 * Pluggable clock abstraction to keep domain services pure.
 */
public interface ClockPort {

    /**
     * @return the current {@link Instant}.
     */
    Instant now();
}
