package com.aequitas.aequitascentralservice.app.port.outbound;

import java.util.Optional;
import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;

/**
 * Outbound port for firm persistence operations.
 */
public interface FirmRepositoryPort {

    /**
     * Persists a firm aggregate.
     *
     * @param firm aggregate to save.
     * @return saved aggregate with any generated fields populated.
     */
    Firm save(Firm firm);

    /**
     * Retrieves a firm by identifier.
     *
     * @param id firm identifier.
     * @return optional firm if found.
     */
    Optional<Firm> findById(UUID id);

    /**
     * Retrieves a paginated list of all firms.
     *
     * @param pageRequest pagination parameters.
     * @return page of firms.
     */
    PageResult<Firm> list(PageRequest pageRequest);
}
