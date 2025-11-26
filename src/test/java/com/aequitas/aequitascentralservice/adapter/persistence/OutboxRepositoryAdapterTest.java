package com.aequitas.aequitascentralservice.adapter.persistence;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.OutboxJpaRepository;
import com.aequitas.aequitascentralservice.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OutboxRepositoryAdapterTest {

    @Mock
    private OutboxJpaRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<OutboxEntity> entityCaptor;

    private OutboxRepositoryAdapter adapter;

    private UUID testFirmId;
    private UUID testAggregateId;
    private UUID testEventId;
    private Instant testOccurredAt;
    private String testEventType;

    @BeforeEach
    void setUp() {
        adapter = new OutboxRepositoryAdapter(repository, objectMapper);
        testFirmId = UUID.randomUUID();
        testAggregateId = UUID.randomUUID();
        testEventId = UUID.randomUUID();
        testOccurredAt = Instant.now();
        testEventType = "TEST_EVENT.v1";
    }

    @Test
    void GIVEN_validDomainEvent_WHEN_append_THEN_savesEntityWithCorrectFields() throws JsonProcessingException {
        // GIVEN
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, testEventType);
        String expectedJson = "{\"eventId\":\"" + testEventId + "\",\"eventType\":\"" + testEventType + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        // WHEN
        adapter.append(testFirmId, testAggregateId, event);

        // THEN
        verify(objectMapper).writeValueAsString(event);
        verify(repository).save(entityCaptor.capture());
        verifyNoMoreInteractions(objectMapper, repository);

        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity).isNotNull();
        assertThat(capturedEntity.getId()).isEqualTo(testEventId);
        assertThat(capturedEntity.getFirmId()).isEqualTo(testFirmId);
        assertThat(capturedEntity.getAggregateId()).isEqualTo(testAggregateId);
        assertThat(capturedEntity.getEventType()).isEqualTo(testEventType);
        assertThat(capturedEntity.getEventKey()).isEqualTo(testAggregateId + "::" + testEventType);
        assertThat(capturedEntity.getPayloadJson()).isEqualTo(expectedJson);
        assertThat(capturedEntity.getOccurredAt()).isEqualTo(testOccurredAt);
        assertThat(capturedEntity.getPublishedAt()).isNull();
    }

    @Test
    void GIVEN_jsonSerializationFailure_WHEN_append_THEN_throwsIllegalStateException() throws JsonProcessingException {
        // GIVEN
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, testEventType);
        JsonProcessingException jsonException = new JsonProcessingException("Serialization failed") {};

        when(objectMapper.writeValueAsString(event)).thenThrow(jsonException);

        // WHEN / THEN
        assertThatThrownBy(() -> adapter.append(testFirmId, testAggregateId, event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to serialize event payload")
                .hasCause(jsonException);

        verify(objectMapper).writeValueAsString(event);
        verifyNoMoreInteractions(objectMapper, repository);
    }

    @Test
    void GIVEN_differentEventType_WHEN_append_THEN_constructsCorrectEventKey() throws JsonProcessingException {
        // GIVEN
        String differentEventType = "ENTRY_APPROVED.v1";
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, differentEventType);
        String expectedJson = "{\"eventId\":\"" + testEventId + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        // WHEN
        adapter.append(testFirmId, testAggregateId, event);

        // THEN
        verify(repository).save(entityCaptor.capture());
        
        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getEventKey()).isEqualTo(testAggregateId + "::" + differentEventType);
        assertThat(capturedEntity.getEventType()).isEqualTo(differentEventType);
    }

    @Test
    void GIVEN_complexJsonPayload_WHEN_append_THEN_savesCompleteJsonString() throws JsonProcessingException {
        // GIVEN
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, testEventType);
        String complexJson = "{\"eventId\":\"" + testEventId + "\",\"eventType\":\"" + testEventType + 
                           "\",\"nested\":{\"field1\":\"value1\",\"field2\":123}}";

        when(objectMapper.writeValueAsString(event)).thenReturn(complexJson);

        // WHEN
        adapter.append(testFirmId, testAggregateId, event);

        // THEN
        verify(repository).save(entityCaptor.capture());
        
        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getPayloadJson()).isEqualTo(complexJson);
        assertThat(capturedEntity.getPayloadJson()).contains("nested");
        assertThat(capturedEntity.getPayloadJson()).contains("field1");
    }

    @Test
    void GIVEN_differentFirmId_WHEN_append_THEN_savesWithCorrectFirmId() throws JsonProcessingException {
        // GIVEN
        UUID differentFirmId = UUID.randomUUID();
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, testEventType);
        String expectedJson = "{\"eventId\":\"" + testEventId + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        // WHEN
        adapter.append(differentFirmId, testAggregateId, event);

        // THEN
        verify(repository).save(entityCaptor.capture());
        
        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getFirmId()).isEqualTo(differentFirmId);
    }

    @Test
    void GIVEN_differentAggregateId_WHEN_append_THEN_savesWithCorrectAggregateIdAndUpdatesEventKey() throws JsonProcessingException {
        // GIVEN
        UUID differentAggregateId = UUID.randomUUID();
        DomainEvent event = createTestEvent(testEventId, testOccurredAt, testEventType);
        String expectedJson = "{\"eventId\":\"" + testEventId + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        // WHEN
        adapter.append(testFirmId, differentAggregateId, event);

        // THEN
        verify(repository).save(entityCaptor.capture());
        
        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getAggregateId()).isEqualTo(differentAggregateId);
        assertThat(capturedEntity.getEventKey()).isEqualTo(differentAggregateId + "::" + testEventType);
    }

    @Test
    void GIVEN_eventWithDifferentTimestamp_WHEN_append_THEN_savesWithCorrectOccurredAt() throws JsonProcessingException {
        // GIVEN
        Instant differentTimestamp = Instant.parse("2025-01-15T10:30:00Z");
        DomainEvent event = createTestEvent(testEventId, differentTimestamp, testEventType);
        String expectedJson = "{\"eventId\":\"" + testEventId + "\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        // WHEN
        adapter.append(testFirmId, testAggregateId, event);

        // THEN
        verify(repository).save(entityCaptor.capture());
        
        OutboxEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getOccurredAt()).isEqualTo(differentTimestamp);
    }

    @Test
    void GIVEN_multipleEvents_WHEN_append_THEN_eachEventIsSavedIndependently() throws JsonProcessingException {
        // GIVEN
        DomainEvent event1 = createTestEvent(UUID.randomUUID(), Instant.now(), "EVENT_1.v1");
        DomainEvent event2 = createTestEvent(UUID.randomUUID(), Instant.now(), "EVENT_2.v1");
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // WHEN
        adapter.append(testFirmId, testAggregateId, event1);
        adapter.append(testFirmId, testAggregateId, event2);

        // THEN
        verify(objectMapper).writeValueAsString(event1);
        verify(objectMapper).writeValueAsString(event2);
        verify(repository, times(2)).save(any(OutboxEntity.class));
        verifyNoMoreInteractions(objectMapper, repository);
    }

    /**
     * Helper method to create a test DomainEvent.
     */
    private DomainEvent createTestEvent(UUID eventId, Instant occurredAt, String eventType) {
        return new DomainEvent() {
            @Override
            public UUID eventId() {
                return eventId;
            }

            @Override
            public Instant occurredAt() {
                return occurredAt;
            }

            @Override
            public String eventType() {
                return eventType;
            }
        };
    }
}
