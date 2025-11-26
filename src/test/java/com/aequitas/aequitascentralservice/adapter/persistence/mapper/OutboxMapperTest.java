package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import com.aequitas.aequitascentralservice.domain.model.OutboxMessage;

class OutboxMapperTest {

    @Test
    void GIVEN_validOutboxMessage_WHEN_toEntity_THEN_mapsAllFieldsCorrectly() {
        // GIVEN
        UUID id = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String eventType = "ProjectCreated";
        String payloadJson = "{\"data\":\"test\"}";
        Instant occurredAt = Instant.parse("2025-01-15T10:30:00Z");
        Instant publishedAt = Instant.parse("2025-01-15T10:31:00Z");

        OutboxMessage message = OutboxMessage.builder()
                .id(id)
                .firmId(firmId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadJson(payloadJson)
                .occurredAt(occurredAt)
                .publishedAt(publishedAt)
                .build();

        // WHEN
        OutboxEntity entity = OutboxMapper.toEntity(message);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getFirmId()).isEqualTo(firmId);
        assertThat(entity.getAggregateId()).isEqualTo(aggregateId);
        assertThat(entity.getEventType()).isEqualTo(eventType);
        assertThat(entity.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(entity.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(entity.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(entity.getEventKey()).isEqualTo(aggregateId + "-" + eventType);
    }

    @Test
    void GIVEN_outboxMessageWithNullPublishedAt_WHEN_toEntity_THEN_mapsNullPublishedAt() {
        // GIVEN
        UUID aggregateId = UUID.randomUUID();
        String eventType = "OrderUpdated";
        
        OutboxMessage message = OutboxMessage.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadJson("{}")
                .occurredAt(Instant.now())
                .publishedAt(null)
                .build();

        // WHEN
        OutboxEntity entity = OutboxMapper.toEntity(message);

        // THEN
        assertThat(entity.getPublishedAt()).isNull();
        assertThat(entity.getEventKey()).isEqualTo(aggregateId + "-" + eventType);
    }

    @Test
    void GIVEN_outboxMessageWithSpecialCharactersInEventType_WHEN_toEntity_THEN_eventKeyIncludesSpecialChars() {
        // GIVEN
        UUID aggregateId = UUID.randomUUID();
        String eventType = "Event.With-Special_Chars";
        
        OutboxMessage message = OutboxMessage.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadJson("{\"key\":\"value\"}")
                .occurredAt(Instant.now())
                .publishedAt(null)
                .build();

        // WHEN
        OutboxEntity entity = OutboxMapper.toEntity(message);

        // THEN
        assertThat(entity.getEventKey()).isEqualTo(aggregateId + "-" + eventType);
    }

    @Test
    void GIVEN_validOutboxEntity_WHEN_toDomain_THEN_mapsAllFieldsCorrectly() {
        // GIVEN
        UUID id = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String eventType = "CustomerDeleted";
        String payloadJson = "{\"customerId\":\"123\"}";
        Instant occurredAt = Instant.parse("2025-02-20T14:45:30Z");
        Instant publishedAt = Instant.parse("2025-02-20T14:46:00Z");

        OutboxEntity entity = OutboxEntity.builder()
                .id(id)
                .firmId(firmId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .eventKey(aggregateId + "-" + eventType)
                .payloadJson(payloadJson)
                .occurredAt(occurredAt)
                .publishedAt(publishedAt)
                .build();

        // WHEN
        OutboxMessage message = OutboxMapper.toDomain(entity);

        // THEN
        assertThat(message).isNotNull();
        assertThat(message.id()).isEqualTo(id);
        assertThat(message.firmId()).isEqualTo(firmId);
        assertThat(message.aggregateId()).isEqualTo(aggregateId);
        assertThat(message.eventType()).isEqualTo(eventType);
        assertThat(message.payloadJson()).isEqualTo(payloadJson);
        assertThat(message.occurredAt()).isEqualTo(occurredAt);
        assertThat(message.publishedAt()).isEqualTo(publishedAt);
    }

    @Test
    void GIVEN_outboxEntityWithNullPublishedAt_WHEN_toDomain_THEN_mapsNullPublishedAt() {
        // GIVEN
        OutboxEntity entity = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .eventType("TestEvent")
                .eventKey("test-key")
                .payloadJson("{}")
                .occurredAt(Instant.now())
                .publishedAt(null)
                .build();

        // WHEN
        OutboxMessage message = OutboxMapper.toDomain(entity);

        // THEN
        assertThat(message.publishedAt()).isNull();
    }

    @Test
    void GIVEN_outboxEntityWithEmptyPayload_WHEN_toDomain_THEN_mapsEmptyPayload() {
        // GIVEN
        OutboxEntity entity = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .eventType("EmptyEvent")
                .eventKey("key")
                .payloadJson("")
                .occurredAt(Instant.now())
                .publishedAt(null)
                .build();

        // WHEN
        OutboxMessage message = OutboxMapper.toDomain(entity);

        // THEN
        assertThat(message.payloadJson()).isEmpty();
    }

    @Test
    void GIVEN_messageAndBackToEntity_WHEN_roundTripConversion_THEN_preservesAllData() {
        // GIVEN
        UUID id = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String eventType = "RoundTripTest";
        String payloadJson = "{\"test\":\"data\"}";
        Instant occurredAt = Instant.parse("2025-03-10T08:00:00Z");
        Instant publishedAt = Instant.parse("2025-03-10T08:01:00Z");

        OutboxMessage originalMessage = OutboxMessage.builder()
                .id(id)
                .firmId(firmId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadJson(payloadJson)
                .occurredAt(occurredAt)
                .publishedAt(publishedAt)
                .build();

        // WHEN
        OutboxEntity entity = OutboxMapper.toEntity(originalMessage);
        OutboxMessage reconvertedMessage = OutboxMapper.toDomain(entity);

        // THEN
        assertThat(reconvertedMessage).isEqualTo(originalMessage);
    }

    @Test
    void GIVEN_twoMessagesWithSameAggregateIdAndEventType_WHEN_toEntity_THEN_generatesSameEventKey() {
        // GIVEN
        UUID sharedAggregateId = UUID.randomUUID();
        String sharedEventType = "SharedEvent";

        OutboxMessage message1 = OutboxMessage.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(sharedAggregateId)
                .eventType(sharedEventType)
                .payloadJson("{\"msg\":1}")
                .occurredAt(Instant.now())
                .build();

        OutboxMessage message2 = OutboxMessage.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .aggregateId(sharedAggregateId)
                .eventType(sharedEventType)
                .payloadJson("{\"msg\":2}")
                .occurredAt(Instant.now())
                .build();

        // WHEN
        OutboxEntity entity1 = OutboxMapper.toEntity(message1);
        OutboxEntity entity2 = OutboxMapper.toEntity(message2);

        // THEN
        assertThat(entity1.getEventKey()).isEqualTo(entity2.getEventKey());
        assertThat(entity1.getEventKey()).isEqualTo(sharedAggregateId + "-" + sharedEventType);
    }
}
