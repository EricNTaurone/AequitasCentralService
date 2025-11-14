package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.TimeEntryMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.TimeEntryJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.TimeEntryRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * JPA-backed implementation of {@link TimeEntryRepositoryPort}.
 */
@Component
public class TimeEntryRepositoryAdapter implements TimeEntryRepositoryPort {

    private final TimeEntryJpaRepository repository;

    public TimeEntryRepositoryAdapter(final TimeEntryJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeEntry save(final TimeEntry entry) {
        final TimeEntryEntity saved = repository.save(TimeEntryMapper.toEntity(entry));
        return TimeEntryMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TimeEntry> findById(final UUID id, final UUID firmId) {
        return repository.findByIdAndFirmId(id, firmId).map(TimeEntryMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<TimeEntry> search(
            final UUID firmId, final TimeEntryFilter filter, final PageRequest pageRequest) {
        final Specification<TimeEntryEntity> specification =
                (root, query, criteriaBuilder) -> {
                    final List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("firmId"), firmId));
                    filter.customerId()
                            .ifPresent(
                                    value ->
                                            predicates.add(
                                                    criteriaBuilder.equal(root.get("customerId"), value)));
                    filter.projectId()
                            .ifPresent(
                                    value ->
                                            predicates.add(
                                                    criteriaBuilder.equal(root.get("projectId"), value)));
                    filter.status()
                            .ifPresent(
                                    value ->
                                            predicates.add(
                                                    criteriaBuilder.equal(root.get("status"), value)));
                    filter.ownerId()
                            .ifPresent(
                                    value ->
                                            predicates.add(
                                                    criteriaBuilder.equal(root.get("userId"), value)));
                    parseCursor(pageRequest.cursor())
                            .ifPresent(
                                    cursor ->
                                            predicates.add(
                                                    criteriaBuilder.greaterThan(root.get("id"), cursor)));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                };

        final org.springframework.data.domain.PageRequest springPageRequest =
                org.springframework.data.domain.PageRequest.of(
                        0, pageRequest.limit(), Sort.by(Sort.Direction.ASC, "id"));

        final var page = repository.findAll(specification, springPageRequest);
        final List<TimeEntry> items =
                page.getContent().stream().map(TimeEntryMapper::toDomain).toList();
        final String nextCursor =
                items.size() == pageRequest.limit()
                                && !page.getContent().isEmpty()
                        ? page.getContent()
                                .get(page.getContent().size() - 1)
                                .getId()
                                .toString()
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
