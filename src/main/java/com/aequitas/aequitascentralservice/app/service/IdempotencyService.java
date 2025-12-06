package com.aequitas.aequitascentralservice.app.service;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.IdempotencyRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.IdempotencyRecord;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public IdempotencyService(
            final IdempotencyRepositoryPort repository,
            final CurrentUserPort currentUserPort,
            final ClockPort clockPort,
            final ObjectMapper objectMapper) {
        this.repository = repository;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the supplied supplier exactly once per idempotency key and returns the stored UUID.
     *
     * @param key client-supplied idempotency key.
     * @param operation logical workflow name.
     * @param payload request payload to hash for validation.
     * @param supplier supplier executed when the key has not been used.
     * @return previously stored or newly created response identifier.
     */
    @Transactional
    public UUID execute(
            final String key,
            final IdempotencyOperation operation,
            final Object payload,
            final Supplier<UUID> supplier) {
        if (key == null || key.isBlank()) {
            return supplier.get();
        }
        final CurrentUser currentUser = currentUserPort.currentUser();
        final String keyHash = hashKey(key);
        final String payloadHash = hashPayload(payload);

        final Optional<IdempotencyRecord> existing =
                repository.find(operation, currentUser.userId(), keyHash);
        final Instant now = clockPort.now();
        if (existing.isPresent() && existing.get().expiresAt().isAfter(now)) {
            if (!existing.get().payloadHash().equals(payloadHash)) {
                throw new IllegalArgumentException("Idempotency key reuse detected with different payload");
            }
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
                        payloadHash,
                        response,
                        now,
                        now.plus(DEFAULT_EXPIRY));
        repository.save(record);
        return response;
    }

    private String hashPayload(final Object payload) {
        if (payload == null) {
            return "empty";
        }
        try {
            final String json = objectMapper.writeValueAsString(payload);
            return hashKey(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash payload", ex);
        }
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
