package com.aequitas.aequitascentralservice.adapter.outbox;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemClockAdapterTest {

  SystemClockAdapter systemClockAdapter;

  @BeforeEach
  void setUp() {
    systemClockAdapter = new SystemClockAdapter();
  }

    @Test
    void GIVEN_systemClock_WHEN_afterWait_THEN_assertAfter() {
        var before = systemClockAdapter.now();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var after = Instant.now();
        assertTrue(after.isAfter(before));
    }

    @Test
    void GIVEN_systemClock_WHEN_beforeWait_THEN_assertBefore() {  
        var before = Instant.now();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var after = systemClockAdapter.now();
        assertTrue(before.isBefore(after));
    }
}
