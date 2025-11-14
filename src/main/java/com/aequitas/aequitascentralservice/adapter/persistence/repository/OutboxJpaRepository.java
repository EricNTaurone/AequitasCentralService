package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.OutboxEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link OutboxEntity}.
 */
public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Fetches the next batch awaiting publication ordered by occurrence date.
     *
     * @param publishedAt picks rows with {@code null} value.
     * @return batch list.
     */
    List<OutboxEntity> findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();

    /**
     * Counts rows older than the provided instant with no published timestamp.
     *
     * @param threshold threshold instant.
     * @return backlog size.
     */
    long countByPublishedAtIsNullAndOccurredAtBefore(Instant threshold);
}
