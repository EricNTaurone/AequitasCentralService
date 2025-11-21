package com.aequitas.aequitascentralservice.adapter.outbox;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.params.ParameterizedTest;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;

@ExtendWith(MockitoExtension.class)
public class OutboxRelayTest {

    private static final String EVENT_TYPE = "TestEvent";
    private static final String PAYLOAD = "{\"key\":\"value\"}";
    private static final String FIRM_ID = UUID.randomUUID().toString();
    private static final String EVENT_KEY = "event-key-123";

    @Mock
    private OutboxJpaRepository outboxJpaRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private OutboxRelay outboxRelay;

    @Test
    void GIVEN_emptyBatch_WHEN_relayIsCalled_THEN_noPublishIsInvoked() {
        // Given
        List<OutboxEntity> emptyBatch = List.of();
        when(outboxJpaRepository.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc())
                .thenReturn(emptyBatch);

        // When
        outboxRelay.relay();

        // Then
        verify(outboxJpaRepository, times(1)).findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
        verify(eventPublisher, never()).publish(anyString(), anyString(), anyString(), anyString());
        verify(clockPort, never()).now();

        verifyNoMoreInteractions(outboxJpaRepository);
        verifyNoInteractions(eventPublisher, clockPort);
    }

    @ParameterizedTest
    @MethodSource("provideBatchSizes")
    void GIVEN_batches_WHEN_relayIsCalled_THEN_publishIsInvokedForEachBatch(int batchSize) {
        // Given
        List<OutboxEntity> batch = createOutboxEntities(batchSize);
        when(outboxJpaRepository.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc())
                .thenReturn(batch);
        // When
        outboxRelay.relay();
        // Then
        verify(outboxJpaRepository, times(1)).findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
        verify(eventPublisher, times(batchSize))
                .publish(anyString(), anyString(), anyString(), anyString());
        verify(clockPort, times(batchSize)).now();
        assertNotNull(batch);
        verify(outboxJpaRepository, times(1)).saveAll(batch);
    }

    static Stream<Arguments> provideBatchSizes() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(5),
                Arguments.of(20),
                Arguments.of(100)
        );
    }

    static List<OutboxEntity> createOutboxEntities(final int count) {
        final List<OutboxEntity> entities = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            final OutboxEntity entity = OutboxEntity.builder()
                    .eventType(EVENT_TYPE)
                    .payloadJson(PAYLOAD)
                    .firmId(UUID.fromString(FIRM_ID))
                    .eventKey(EVENT_KEY + "-" + i)
                    .build();
            entities.add(entity);
        }
        return entities;
    }
}
