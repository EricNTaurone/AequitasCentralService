package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.AuthSession;

/**
 * Inbound port for user authentication flows backed by Supabase.
 */
public interface AuthenticationCommandPort {

    /**
     * Registers a new Supabase identity and persists the corresponding
     * tenant-scoped profile.
     *
     * @param command validated sign-up payload.
     * @return authenticated session containing tokens and persisted profile.
     */
    AuthSession signUp(SignUpCommand command);

    /**
     * Performs password authentication against Supabase and syncs the profile
     * locally.
     *
     * @param command validated sign-in payload.
     * @return authenticated session containing tokens and persisted profile.
     */
    AuthSession signIn(SignInCommand command);
}
