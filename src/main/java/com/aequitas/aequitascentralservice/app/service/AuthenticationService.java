package com.aequitas.aequitascentralservice.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aequitas.aequitascentralservice.app.port.inbound.AuthenticationCommandPort;
import com.aequitas.aequitascentralservice.app.port.outbound.SupabaseAuthPort;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.AuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseAuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;

/**
 * Uses Supabase as the identity provider while persisting tenant-scoped
 * profiles locally.
 */
@Service
@Transactional
public class AuthenticationService implements AuthenticationCommandPort {

    private final SupabaseAuthPort supabaseAuthPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;

    public AuthenticationService(
            final SupabaseAuthPort supabaseAuthPort,
            final UserProfileRepositoryPort userProfileRepositoryPort) {
        this.supabaseAuthPort = supabaseAuthPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
    }

    @Override
    public AuthSession signUp(final SignUpCommand command) {
        final SupabaseUser createdUser = supabaseAuthPort.createUser(command);
        final SupabaseAuthSession session =
                supabaseAuthPort.signIn(new SignInCommand(command.email(), command.password()));
        final SupabaseUser sourceUser = session.user() != null ? session.user() : createdUser;
        final UserProfile profile = persistProfile(sourceUser);
        return new AuthSession(profile, session.tokens());
    }

    @Override
    public AuthSession signIn(final SignInCommand command) {
        final SupabaseAuthSession session = supabaseAuthPort.signIn(command);
        final UserProfile profile = persistProfile(session.user());
        return new AuthSession(profile, session.tokens());
    }

    private UserProfile persistProfile(final SupabaseUser user) {
        final UserProfile profile = new UserProfile(user.domainId(), user.id(), user.firmId(), user.email(), user.role());
        return userProfileRepositoryPort.save(profile);
    }
}
