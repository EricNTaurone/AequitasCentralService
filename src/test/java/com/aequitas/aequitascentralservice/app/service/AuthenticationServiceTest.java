package com.aequitas.aequitascentralservice.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.app.port.outbound.SupabaseAuthPort;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.AuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseAuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.AuthTokens;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final UUID FIRM_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @Mock
    private UserProfileRepositoryPort userProfileRepositoryPort;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthTokens tokens;
    private SupabaseUser supabaseUser;
    private SupabaseAuthSession supabaseAuthSession;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        tokens = new AuthTokens("access", "refresh", 3600, "bearer");
        supabaseUser = new SupabaseUser(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
        supabaseAuthSession = new SupabaseAuthSession(supabaseUser, tokens);
        profile = new UserProfile(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
    }

    @Test
    void signUp_createsSupabaseUser_andPersistsProfile() {
        // GIVEN
        SignUpCommand command = new SignUpCommand(FIRM_ID, EMAIL, PASSWORD, Role.ADMIN);
        when(supabaseAuthPort.createUser(command)).thenReturn(supabaseUser);
        when(supabaseAuthPort.signIn(any(SignInCommand.class))).thenReturn(supabaseAuthSession);
        when(userProfileRepositoryPort.save(profile)).thenReturn(profile);

        // WHEN
        AuthSession session = authenticationService.signUp(command);

        // THEN
        assertNotNull(session);
        assertEquals(profile, session.profile());
        assertEquals(tokens, session.tokens());
        verify(supabaseAuthPort, times(1)).createUser(command);
        verify(supabaseAuthPort, times(1)).signIn(any(SignInCommand.class));
        verify(userProfileRepositoryPort, times(1)).save(profile);
    }

    @Test
    void signIn_authenticates_andPersistsProfile() {
        // GIVEN
        SignInCommand command = new SignInCommand(EMAIL, PASSWORD);
        when(supabaseAuthPort.signIn(command)).thenReturn(supabaseAuthSession);
        when(userProfileRepositoryPort.save(profile)).thenReturn(profile);

        // WHEN
        AuthSession session = authenticationService.signIn(command);

        // THEN
        assertNotNull(session);
        assertEquals(profile, session.profile());
        assertEquals(tokens, session.tokens());
        verify(supabaseAuthPort, times(1)).signIn(command);
        verify(userProfileRepositoryPort, times(1)).save(profile);
    }
}
