package com.aequitas.aequitascentralservice.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.IdempotencyRecordEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.IdempotencyRecordMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.IdempotencyJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotencyRepositoryAdapterTest {

    @Mock
    private IdempotencyJpaRepository repository;

    private IdempotencyRepositoryAdapter adapter;

    private UUID testId;
    private UUID testUserId;
    private UUID testFirmId;
    private UUID testResponseId;
    private String testKeyHash;
    private Instant testCreatedAt;
    private Instant testExpiresAt;

    @BeforeEach
    void setUp() {
        adapter = new IdempotencyRepositoryAdapter(repository);
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testFirmId = UUID.randomUUID();
        testResponseId = UUID.randomUUID();
        testKeyHash = "test-hash-123";
        testCreatedAt = Instant.now();
        testExpiresAt = testCreatedAt.plusSeconds(3600);
    }

    @Test
    void GIVEN_existingRecord_WHEN_find_THEN_returnsRecordWrappedInOptional() {
        // GIVEN
        IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_CREATE;
        IdempotencyRecordEntity entity = IdempotencyRecordEntity.builder()
                .id(testId)
                .operation(operation)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecord domainRecord = IdempotencyRecord.builder()
                .id(testId)
                .operation(operation)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        when(repository.findByOperationAndUserIdAndKeyHash(operation, testUserId, testKeyHash))
                .thenReturn(Optional.of(entity));

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            mapperMock.when(() -> IdempotencyRecordMapper.toDomain(entity))
                    .thenReturn(domainRecord);

            // WHEN
            Optional<IdempotencyRecord> result = adapter.find(operation, testUserId, testKeyHash);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(domainRecord);
            assertThat(result.get().id()).isEqualTo(testId);
            assertThat(result.get().operation()).isEqualTo(operation);
            assertThat(result.get().userId()).isEqualTo(testUserId);
            assertThat(result.get().firmId()).isEqualTo(testFirmId);
            assertThat(result.get().keyHash()).isEqualTo(testKeyHash);
            assertThat(result.get().responseId()).isEqualTo(testResponseId);

            verify(repository).findByOperationAndUserIdAndKeyHash(operation, testUserId, testKeyHash);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> IdempotencyRecordMapper.toDomain(entity));
        }
    }

    @Test
    void GIVEN_noExistingRecord_WHEN_find_THEN_returnsEmptyOptional() {
        // GIVEN
        IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_APPROVE;
        when(repository.findByOperationAndUserIdAndKeyHash(operation, testUserId, testKeyHash))
                .thenReturn(Optional.empty());

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            // WHEN
            Optional<IdempotencyRecord> result = adapter.find(operation, testUserId, testKeyHash);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByOperationAndUserIdAndKeyHash(operation, testUserId, testKeyHash);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_validRecord_WHEN_save_THEN_persistsAndReturnsDomainRecord() {
        // GIVEN
        IdempotencyRecord inputRecord = IdempotencyRecord.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_CREATE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecordEntity inputEntity = IdempotencyRecordEntity.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_CREATE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecordEntity savedEntity = IdempotencyRecordEntity.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_CREATE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecord savedRecord = IdempotencyRecord.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_CREATE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(savedEntity);

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            mapperMock.when(() -> IdempotencyRecordMapper.toEntity(inputRecord))
                    .thenReturn(inputEntity);
            mapperMock.when(() -> IdempotencyRecordMapper.toDomain(savedEntity))
                    .thenReturn(savedRecord);

            // WHEN
            IdempotencyRecord result = adapter.save(inputRecord);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedRecord);
            assertThat(result.id()).isEqualTo(testId);
            assertThat(result.operation()).isEqualTo(IdempotencyOperation.TIME_ENTRY_CREATE);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.firmId()).isEqualTo(testFirmId);
            assertThat(result.keyHash()).isEqualTo(testKeyHash);
            assertThat(result.responseId()).isEqualTo(testResponseId);
            assertThat(result.createdAt()).isEqualTo(testCreatedAt);
            assertThat(result.expiresAt()).isEqualTo(testExpiresAt);

            verify(repository).save(inputEntity);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> IdempotencyRecordMapper.toEntity(inputRecord));
            mapperMock.verify(() -> IdempotencyRecordMapper.toDomain(savedEntity));
        }
    }

    @Test
    void GIVEN_recordWithApproveOperation_WHEN_save_THEN_persistsSuccessfully() {
        // GIVEN
        IdempotencyRecord inputRecord = IdempotencyRecord.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecordEntity inputEntity = IdempotencyRecordEntity.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecordEntity savedEntity = IdempotencyRecordEntity.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        IdempotencyRecord savedRecord = IdempotencyRecord.builder()
                .id(testId)
                .operation(IdempotencyOperation.TIME_ENTRY_APPROVE)
                .userId(testUserId)
                .firmId(testFirmId)
                .keyHash(testKeyHash)
                .responseId(testResponseId)
                .createdAt(testCreatedAt)
                .expiresAt(testExpiresAt)
                .build();

        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(savedEntity);

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            mapperMock.when(() -> IdempotencyRecordMapper.toEntity(inputRecord))
                    .thenReturn(inputEntity);
            mapperMock.when(() -> IdempotencyRecordMapper.toDomain(savedEntity))
                    .thenReturn(savedRecord);

            // WHEN
            IdempotencyRecord result = adapter.save(inputRecord);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.operation()).isEqualTo(IdempotencyOperation.TIME_ENTRY_APPROVE);

            verify(repository).save(inputEntity);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> IdempotencyRecordMapper.toEntity(inputRecord));
            mapperMock.verify(() -> IdempotencyRecordMapper.toDomain(savedEntity));
        }
    }

    @Test
    void GIVEN_differentKeyHash_WHEN_find_THEN_queriesWithCorrectParameters() {
        // GIVEN
        String differentKeyHash = "different-hash-456";
        IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_CREATE;
        when(repository.findByOperationAndUserIdAndKeyHash(operation, testUserId, differentKeyHash))
                .thenReturn(Optional.empty());

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            // WHEN
            Optional<IdempotencyRecord> result = adapter.find(operation, testUserId, differentKeyHash);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByOperationAndUserIdAndKeyHash(operation, testUserId, differentKeyHash);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_differentUserId_WHEN_find_THEN_queriesWithCorrectUserId() {
        // GIVEN
        UUID differentUserId = UUID.randomUUID();
        IdempotencyOperation operation = IdempotencyOperation.TIME_ENTRY_APPROVE;
        when(repository.findByOperationAndUserIdAndKeyHash(operation, differentUserId, testKeyHash))
                .thenReturn(Optional.empty());

        try (MockedStatic<IdempotencyRecordMapper> mapperMock = mockStatic(IdempotencyRecordMapper.class)) {
            // WHEN
            Optional<IdempotencyRecord> result = adapter.find(operation, differentUserId, testKeyHash);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByOperationAndUserIdAndKeyHash(operation, differentUserId, testKeyHash);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }
}
