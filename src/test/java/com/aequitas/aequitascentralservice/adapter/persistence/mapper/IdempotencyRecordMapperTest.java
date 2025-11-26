package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.IdempotencyRecordEntity;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;

/**
 * Tests for {@link IdempotencyRecordMapper}.
 */
class IdempotencyRecordMapperTest {

    @Test
    void GIVEN_validEntity_WHEN_toDomain_THEN_returnsDomainRecordWithAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_CREATE;
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String keyHash = "test-key-hash";
        final UUID responseId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plusSeconds(3600);

        final IdempotencyRecordEntity entity = IdempotencyRecordEntity.builder()
                .id(id)
                .operation(operation)
                .userId(userId)
                .firmId(firmId)
                .keyHash(keyHash)
                .responseId(responseId)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // WHEN
        final IdempotencyRecord result = IdempotencyRecordMapper.toDomain(entity);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.operation()).isEqualTo(operation);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.firmId()).isEqualTo(firmId);
        assertThat(result.keyHash()).isEqualTo(keyHash);
        assertThat(result.responseId()).isEqualTo(responseId);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void GIVEN_entityWithApproveOperation_WHEN_toDomain_THEN_returnsDomainWithApproveOperation() {
        // GIVEN
        final IdempotencyRecordEntity entity = IdempotencyRecordEntity.builder()
                .id(UUID.randomUUID())
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .keyHash("approve-key")
                .responseId(UUID.randomUUID())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(7200))
                .build();

        // WHEN
        final IdempotencyRecord result = IdempotencyRecordMapper.toDomain(entity);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.operation()).isEqualTo(IdempotencyOperation.TIME_ENTRY_APPROVE);
    }

    @Test
    void GIVEN_validDomainRecord_WHEN_toEntity_THEN_returnsEntityWithAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_CREATE;
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String keyHash = "domain-key-hash";
        final UUID responseId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plusSeconds(3600);

        final IdempotencyRecord record = IdempotencyRecord.builder()
                .id(id)
                .operation(operation)
                .userId(userId)
                .firmId(firmId)
                .keyHash(keyHash)
                .responseId(responseId)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // WHEN
        final IdempotencyRecordEntity result = IdempotencyRecordMapper.toEntity(record);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getOperation()).isEqualTo(operation);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getFirmId()).isEqualTo(firmId);
        assertThat(result.getKeyHash()).isEqualTo(keyHash);
        assertThat(result.getResponseId()).isEqualTo(responseId);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void GIVEN_domainWithApproveOperation_WHEN_toEntity_THEN_returnsEntityWithApproveOperation() {
        // GIVEN
        final IdempotencyRecord record = IdempotencyRecord.builder()
                .id(UUID.randomUUID())
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .keyHash("approve-domain-key")
                .responseId(UUID.randomUUID())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(7200))
                .build();

        // WHEN
        final IdempotencyRecordEntity result = IdempotencyRecordMapper.toEntity(record);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getOperation()).isEqualTo(IdempotencyOperation.TIME_ENTRY_APPROVE);
    }

    @Test
    void GIVEN_entityToDomainAndBack_WHEN_roundTrip_THEN_preservesAllData() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_CREATE;
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String keyHash = "round-trip-key";
        final UUID responseId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plusSeconds(3600);

        final IdempotencyRecordEntity originalEntity = IdempotencyRecordEntity.builder()
                .id(id)
                .operation(operation)
                .userId(userId)
                .firmId(firmId)
                .keyHash(keyHash)
                .responseId(responseId)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // WHEN
        final IdempotencyRecord domain = IdempotencyRecordMapper.toDomain(originalEntity);
        final IdempotencyRecordEntity resultEntity = IdempotencyRecordMapper.toEntity(domain);

        // THEN
        assertThat(resultEntity.getId()).isEqualTo(originalEntity.getId());
        assertThat(resultEntity.getOperation()).isEqualTo(originalEntity.getOperation());
        assertThat(resultEntity.getUserId()).isEqualTo(originalEntity.getUserId());
        assertThat(resultEntity.getFirmId()).isEqualTo(originalEntity.getFirmId());
        assertThat(resultEntity.getKeyHash()).isEqualTo(originalEntity.getKeyHash());
        assertThat(resultEntity.getResponseId()).isEqualTo(originalEntity.getResponseId());
        assertThat(resultEntity.getCreatedAt()).isEqualTo(originalEntity.getCreatedAt());
        assertThat(resultEntity.getExpiresAt()).isEqualTo(originalEntity.getExpiresAt());
    }

    @Test
    void GIVEN_domainToEntityAndBack_WHEN_roundTrip_THEN_preservesAllData() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_APPROVE;
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String keyHash = "reverse-round-trip-key";
        final UUID responseId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plusSeconds(7200);

        final IdempotencyRecord originalRecord = IdempotencyRecord.builder()
                .id(id)
                .operation(operation)
                .userId(userId)
                .firmId(firmId)
                .keyHash(keyHash)
                .responseId(responseId)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        // WHEN
        final IdempotencyRecordEntity entity = IdempotencyRecordMapper.toEntity(originalRecord);
        final IdempotencyRecord resultRecord = IdempotencyRecordMapper.toDomain(entity);

        // THEN
        assertThat(resultRecord.id()).isEqualTo(originalRecord.id());
        assertThat(resultRecord.operation()).isEqualTo(originalRecord.operation());
        assertThat(resultRecord.userId()).isEqualTo(originalRecord.userId());
        assertThat(resultRecord.firmId()).isEqualTo(originalRecord.firmId());
        assertThat(resultRecord.keyHash()).isEqualTo(originalRecord.keyHash());
        assertThat(resultRecord.responseId()).isEqualTo(originalRecord.responseId());
        assertThat(resultRecord.createdAt()).isEqualTo(originalRecord.createdAt());
        assertThat(resultRecord.expiresAt()).isEqualTo(originalRecord.expiresAt());
    }
}
