package com.aequitas.aequitascentralservice.tenancy;

import com.aequitas.aequitascentralservice.domain.value.CurrentUser;

/**
 * Thread-local holder for the authenticated tenant context.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    /**
     * Stores the supplied user for the current thread.
     *
     * @param user authenticated principal.
     */
    public static void setCurrentUser(final CurrentUser user) {
        CONTEXT.set(user);
    }

    /**
     * Retrieves the stored user if present.
     *
     * @return optional current user.
     */
    public static CurrentUser getCurrentUser() {
        return CONTEXT.get();
    }

    /**
     * Clears the context to avoid leaking tenants across requests.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
