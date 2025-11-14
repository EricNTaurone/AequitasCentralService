package com.aequitas.aequitascentralservice.adapter.outbox;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Default {@link ClockPort} backed by {@link Clock#systemUTC()}.
 */
@Component
public class SystemClockAdapter implements ClockPort {

    private final Clock clock = Clock.systemUTC();

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant now() {
        return clock.instant();
    }
}
