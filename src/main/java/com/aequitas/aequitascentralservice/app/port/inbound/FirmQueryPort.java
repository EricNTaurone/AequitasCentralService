package com.aequitas.aequitascentralservice.app.port.inbound;

import java.util.Optional;
import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;

/**
 * Inbound port for read-side firm operations.
 */
public interface FirmQueryPort {

    /**
     * Retrieves the authenticated user's firm.
     *
     * @return the firm associated with the current user.
     */
    Firm getCurrentUserFirm();

    /**
     * Fetches a single firm by identifier.
     *
     * @param id firm identifier.
     * @return optional firm.
     */
    Optional<Firm> findById(UUID id);

    /**
     * Retrieves a paginated list of all firms.
     *
     * @param pageRequest pagination primitives.
     * @return page of firms.
     */
    PageResult<Firm> list(PageRequest pageRequest);
}
