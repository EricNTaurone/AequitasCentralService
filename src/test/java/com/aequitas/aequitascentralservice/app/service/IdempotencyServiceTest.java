package com.aequitas.aequitascentralservice.app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.IdempotencyRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import com.aequitas.aequitascentralservice.domain.value.Role;

/**
 * Tests for {@link IdempotencyService}.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_FIRM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TEST_RECORD_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TEST_CACHED_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TEST_NEW_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant TEST_NOW = Instant.parse("2024-01-01T00:00:00Z");
    private static final String TEST_KEY = "hash";
    private static final String TEST_KEY_2 = "key-123";
    private static final IdempotencyOperation TEST_OPERATION = IdempotencyOperation.TIME_ENTRY_CREATE;
    private static final IdempotencyOperation TEST_OPERATION_2 = IdempotencyOperation.TIME_ENTRY_APPROVE;

    @Captor
    private ArgumentCaptor<IdempotencyRecord> recordCaptor;

    @Mock
    private IdempotencyRepositoryPort repositoryPort;
    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private IdempotencyService service;

    @Test
    void GIVEN_existingValidRecord_WHEN_execute_THEN_returnCachedResponse() {
        // GIVEN
        CurrentUser currentUser = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.EMPLOYEE);
        String hashedKey = hashKey(TEST_KEY);
        IdempotencyRecord record =
                new IdempotencyRecord(
                        TEST_RECORD_ID,
                        TEST_OPERATION,
                        currentUser.userId(),
                        currentUser.firmId(),
                        hashedKey,
                        TEST_CACHED_ID,
                        TEST_NOW,
                        TEST_NOW.plusSeconds(3600));
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(clockPort.now()).thenReturn(TEST_NOW);
        when(repositoryPort.find(TEST_OPERATION, currentUser.userId(), hashedKey))
                .thenReturn(Optional.of(record));

        // WHEN
        UUID result = service.execute(
                TEST_KEY,
                TEST_OPERATION,
                () -> {
                    throw new IllegalStateException("should not be invoked");
                });

        // THEN
        assertEquals(TEST_CACHED_ID, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(clockPort, times(1)).now();
        verify(repositoryPort, times(1))
                .find(TEST_OPERATION, currentUser.userId(), hashedKey);
        verify(repositoryPort, never()).save(recordCaptor.capture());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_noExistingRecord_WHEN_execute_THEN_persistAndReturnNewResponse() {
        // GIVEN
        CurrentUser currentUser = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.EMPLOYEE);
        String hashedKey = hashKey(TEST_KEY_2);
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(clockPort.now()).thenReturn(TEST_NOW);
        when(repositoryPort.find(TEST_OPERATION_2, currentUser.userId(), hashedKey))
                .thenReturn(Optional.empty());

        // WHEN
        UUID result =
                service.execute(
                        TEST_KEY_2,
                        TEST_OPERATION_2,
                        () -> TEST_NEW_ID);

        // THEN
        assertEquals(TEST_NEW_ID, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(clockPort, times(1)).now();
        verify(repositoryPort, times(1))
                .find(TEST_OPERATION_2, currentUser.userId(), hashedKey);
        verify(repositoryPort, times(1)).save(recordCaptor.capture());
        IdempotencyRecord savedRecord = recordCaptor.getValue();
        assertNotNull(savedRecord.id());
        assertEquals(TEST_OPERATION_2, savedRecord.operation());
        assertEquals(currentUser.userId(), savedRecord.userId());
        assertEquals(currentUser.firmId(), savedRecord.firmId());
        assertEquals(hashedKey, savedRecord.keyHash());
        assertSame(TEST_NEW_ID, savedRecord.responseId());
        assertEquals(TEST_NOW, savedRecord.createdAt());
        assertEquals(TEST_NOW.plusSeconds(86400), savedRecord.expiresAt());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_expiredRecord_WHEN_execute_THEN_persistAndReturnNewResponse() {
        // GIVEN
        CurrentUser currentUser = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.EMPLOYEE);
        String hashedKey = hashKey(TEST_KEY);
        Instant expiredTime = TEST_NOW.minusSeconds(3600);
        IdempotencyRecord expiredRecord =
                new IdempotencyRecord(
                        TEST_RECORD_ID,
                        TEST_OPERATION,
                        currentUser.userId(),
                        currentUser.firmId(),
                        hashedKey,
                        TEST_CACHED_ID,
                        expiredTime.minusSeconds(86400),
                        expiredTime);
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(clockPort.now()).thenReturn(TEST_NOW);
        when(repositoryPort.find(TEST_OPERATION, currentUser.userId(), hashedKey))
                .thenReturn(Optional.of(expiredRecord));

        // WHEN
        UUID result = service.execute(TEST_KEY, TEST_OPERATION, () -> TEST_NEW_ID);

        // THEN
        assertEquals(TEST_NEW_ID, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(clockPort, times(1)).now();
        verify(repositoryPort, times(1))
                .find(TEST_OPERATION, currentUser.userId(), hashedKey);
        verify(repositoryPort, times(1)).save(recordCaptor.capture());
        IdempotencyRecord savedRecord = recordCaptor.getValue();
        assertNotNull(savedRecord.id());
        assertEquals(TEST_OPERATION, savedRecord.operation());
        assertEquals(currentUser.userId(), savedRecord.userId());
        assertEquals(currentUser.firmId(), savedRecord.firmId());
        assertEquals(hashedKey, savedRecord.keyHash());
        assertSame(TEST_NEW_ID, savedRecord.responseId());
        assertEquals(TEST_NOW, savedRecord.createdAt());
        assertEquals(TEST_NOW.plusSeconds(86400), savedRecord.expiresAt());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_nullKey_WHEN_execute_THEN_invokeSupplierWithoutPersistence() {
        // GIVEN
        // No mocking needed - null key bypasses all port calls

        // WHEN
        UUID result = service.execute(null, TEST_OPERATION, () -> TEST_NEW_ID);

        // THEN
        assertEquals(TEST_NEW_ID, result);
        verify(currentUserPort, never()).currentUser();
        verify(clockPort, never()).now();
        verify(repositoryPort, never()).find(TEST_OPERATION, TEST_USER_ID, null);
        verify(repositoryPort, never()).save(recordCaptor.capture());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_blankKey_WHEN_execute_THEN_invokeSupplierWithoutPersistence() {
        // GIVEN
        String blankKey = "   ";

        // WHEN
        UUID result = service.execute(blankKey, TEST_OPERATION, () -> TEST_NEW_ID);

        // THEN
        assertEquals(TEST_NEW_ID, result);
        verify(currentUserPort, never()).currentUser();
        verify(clockPort, never()).now();
        verify(repositoryPort, never()).find(TEST_OPERATION, TEST_USER_ID, hashKey(blankKey));
        verify(repositoryPort, never()).save(recordCaptor.capture());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_emptyKey_WHEN_execute_THEN_invokeSupplierWithoutPersistence() {
        // GIVEN
        String emptyKey = "";

        // WHEN
        UUID result = service.execute(emptyKey, TEST_OPERATION, () -> TEST_NEW_ID);

        // THEN
        assertEquals(TEST_NEW_ID, result);
        verify(currentUserPort, never()).currentUser();
        verify(clockPort, never()).now();
        verify(repositoryPort, never()).find(TEST_OPERATION, TEST_USER_ID, hashKey(emptyKey));
        verify(repositoryPort, never()).save(recordCaptor.capture());
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    private static String hashKey(final String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
