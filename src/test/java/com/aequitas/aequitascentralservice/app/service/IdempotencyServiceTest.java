package com.aequitas.aequitascentralservice.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.IdempotencyRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link IdempotencyService}.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock private IdempotencyRepositoryPort repositoryPort;
    @Mock private CurrentUserPort currentUserPort;
    @Mock private ClockPort clockPort;

    private IdempotencyService service;
    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        service = new IdempotencyService(repositoryPort, currentUserPort, clockPort);
        currentUser = new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Role.EMPLOYEE);
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(clockPort.now()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("execute should short-circuit when key already seen")
    void executeReturnsCachedResponse() {
        // GIVEN
        UUID cachedId = UUID.randomUUID();
        String hashedKey = hashKey("hash");
        IdempotencyRecord record =
                new IdempotencyRecord(
                        UUID.randomUUID(),
                        IdempotencyOperation.TIME_ENTRY_CREATE,
                        currentUser.userId(),
                        currentUser.firmId(),
                        hashedKey,
                        cachedId,
                        Instant.now(),
                        Instant.now().plusSeconds(3600));
        when(repositoryPort.find(IdempotencyOperation.TIME_ENTRY_CREATE, currentUser.userId(), hashedKey))
                .thenReturn(Optional.of(record));

        // WHEN
        UUID result = service.execute(
                "hash",
                IdempotencyOperation.TIME_ENTRY_CREATE,
                () -> {
                    throw new IllegalStateException("should not be invoked");
                });

        // THEN
        assertEquals(cachedId, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(clockPort, times(1)).now();
        verify(repositoryPort, times(1))
                .find(IdempotencyOperation.TIME_ENTRY_CREATE, currentUser.userId(), hashedKey);
        verifyNoMoreInteractions(repositoryPort, currentUserPort, clockPort);
    }

    @Test
    @DisplayName("execute should persist when supplier completes successfully")
    void executePersists() {
        // GIVEN
        UUID newId = UUID.randomUUID();
        String hashedKey = hashKey("key-123");
        when(repositoryPort.find(
                        IdempotencyOperation.TIME_ENTRY_APPROVE, currentUser.userId(), hashedKey))
                .thenReturn(Optional.empty());
        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);

        // WHEN
        UUID result =
                service.execute(
                        "key-123",
                        IdempotencyOperation.TIME_ENTRY_APPROVE,
                        () -> newId);

        // THEN
        assertEquals(newId, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(clockPort, times(1)).now();
        verify(repositoryPort, times(1))
                .find(IdempotencyOperation.TIME_ENTRY_APPROVE, currentUser.userId(), hashedKey);
        verify(repositoryPort, times(1)).save(captor.capture());
        assertSame(newId, captor.getValue().responseId());
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
