package com.aequitas.aequitascentralservice.adapter.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.FirmEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.FirmMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.FirmJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.FirmRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;

import jakarta.persistence.criteria.Predicate;

/**
 * JPA-backed implementation of {@link FirmRepositoryPort}.
 * 
 * <p>This adapter translates between the domain layer's {@link Firm} aggregates
 * and the persistence layer's {@link FirmEntity} via {@link FirmMapper}.
 * 
 * <p><strong>Pagination Strategy:</strong> Uses cursor-based pagination with UUID
 * ordering for stable, consistent results across large datasets. The cursor encodes
 * the last-seen firm ID, and subsequent queries fetch records with IDs greater than
 * the cursor value.
 * 
 * <p><strong>Thread-Safety:</strong> This adapter is stateless and thread-safe;
 * Spring manages it as a singleton. The underlying {@link FirmJpaRepository} is
 * thread-safe per Spring Data JPA specifications.
 * 
 * @see FirmRepositoryPort
 * @see FirmMapper
 * @see FirmJpaRepository
 * @since 1.0
 */
@Component
public class FirmRepositoryAdapter implements FirmRepositoryPort {

    private final FirmJpaRepository repository;

    public FirmRepositoryAdapter(final FirmJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Firm save(final Firm firm) {
        final FirmEntity entity = FirmMapper.toEntity(firm);
        final FirmEntity saved = repository.save(entity);
        return FirmMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Firm> findById(final UUID id) {
        return repository.findById(id).map(FirmMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<Firm> list(final PageRequest pageRequest) {
        final Specification<FirmEntity> specification = (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            parseCursor(pageRequest.cursor())
                    .ifPresent(
                            cursor ->
                                    predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        final org.springframework.data.domain.PageRequest springPageRequest =
                org.springframework.data.domain.PageRequest.of(
                        0, pageRequest.limit(), Sort.by(Sort.Direction.ASC, "id"));

        final var page = repository.findAll(specification, springPageRequest);
        final List<Firm> items = page.getContent().stream()
                .map(FirmMapper::toDomain)
                .toList();
        final String nextCursor = items.size() == pageRequest.limit() && !page.getContent().isEmpty()
                ? page.getContent().get(page.getContent().size() - 1).getId().toString()
                : null;
        return new PageResult<>(items, nextCursor, page.getTotalElements(), nextCursor != null);
    }

    private Optional<UUID> parseCursor(final String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(cursor));
    }
}
