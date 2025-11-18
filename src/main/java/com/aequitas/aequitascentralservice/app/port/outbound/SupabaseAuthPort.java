package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.SupabaseAuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;

/**
 * Outbound port abstracting Supabase authentication and user management APIs.
 */
public interface SupabaseAuthPort {

    /**
     * Creates a Supabase user with domain metadata.
     *
     * @param command sign-up payload containing firm and role.
     * @return resulting Supabase user.
     */
    SupabaseUser createUser(SignUpCommand command);

    /**
     * Performs password-based authentication against Supabase.
     *
     * @param command credentials to validate.
     * @return authenticated Supabase user plus issued tokens.
     */
    SupabaseAuthSession signIn(SignInCommand command);
}
