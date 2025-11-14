package com.aequitas.aequitascentralservice.app.service;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.IdempotencyRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates storage and replay of idempotent workflow results.
 */
@Service
public class IdempotencyService {

    private static final Duration DEFAULT_EXPIRY = Duration.ofHours(24);

    private final IdempotencyRepositoryPort repository;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;

    public IdempotencyService(
            final IdempotencyRepositoryPort repository,
            final CurrentUserPort currentUserPort,
            final ClockPort clockPort) {
        this.repository = repository;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
    }

    /**
     * Executes the supplied supplier exactly once per idempotency key and returns the stored UUID.
     *
     * @param key client-supplied idempotency key.
     * @param operation logical workflow name.
     * @param supplier supplier executed when the key has not been used.
     * @return previously stored or newly created response identifier.
     */
    @Transactional
    public UUID execute(final String key, final IdempotencyOperation operation, final Supplier<UUID> supplier) {
        if (key == null || key.isBlank()) {
            return supplier.get();
        }
        final CurrentUser currentUser = currentUserPort.currentUser();
        final String keyHash = hashKey(key);
        final Optional<IdempotencyRecord> existing =
                repository.find(operation, currentUser.userId(), keyHash);
        final Instant now = clockPort.now();
        if (existing.isPresent() && existing.get().expiresAt().isAfter(now)) {
            return existing.get().responseId();
        }
        final UUID response = supplier.get();
        final IdempotencyRecord record =
                new IdempotencyRecord(
                        UUID.randomUUID(),
                        operation,
                        currentUser.userId(),
                        currentUser.firmId(),
                        keyHash,
                        response,
                        now,
                        now.plus(DEFAULT_EXPIRY));
        repository.save(record);
        return response;
    }

    private static String hashKey(final String key) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(key.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash idempotency key", ex);
        }
    }
}
