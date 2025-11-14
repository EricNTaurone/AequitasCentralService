package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.value.CurrentUser;

/**
 * Supplies the authenticated principal to domain services.
 */
public interface CurrentUserPort {

    /**
     * Retrieves the current authenticated user.
     *
     * @return immutable snapshot of the principal.
     */
    CurrentUser currentUser();
}
