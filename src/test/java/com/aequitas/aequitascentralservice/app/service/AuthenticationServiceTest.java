package com.aequitas.aequitascentralservice.app.service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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

    @Captor
    private ArgumentCaptor<SignInCommand> signInCommandCaptor;

    @Captor
    private ArgumentCaptor<UserProfile> userProfileCaptor;

    private AuthTokens tokens;
    private SupabaseUser supabaseUser;
    private SupabaseAuthSession supabaseAuthSession;
    private UserProfile expectedProfile;

    @BeforeEach
    void setUp() {
        tokens = new AuthTokens("access", "refresh", 3600, "bearer");
        supabaseUser = new SupabaseUser(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
        supabaseAuthSession = new SupabaseAuthSession(supabaseUser, tokens);
        expectedProfile = new UserProfile(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
    }

    @Test
    void GIVEN_validSignUpCommand_WHEN_signUp_THEN_createsUserAndReturnsAuthSession() {
        // GIVEN
        SignUpCommand command = new SignUpCommand(FIRM_ID, EMAIL, PASSWORD, Role.ADMIN);
        
        // Create a createdUser with one set of attributes
        UUID createdUserId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        SupabaseUser createdUser = new SupabaseUser(createdUserId, FIRM_ID, "created@example.com", Role.MANAGER);
        
        // Session user has DIFFERENT attributes - this is what should be used when session.user() != null
        SupabaseUser sessionUser = new SupabaseUser(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
        SupabaseAuthSession session = new SupabaseAuthSession(sessionUser, tokens);
        
        UserProfile expectedSessionProfile = new UserProfile(USER_ID, FIRM_ID, EMAIL, Role.ADMIN);
        
        when(supabaseAuthPort.createUser(command)).thenReturn(createdUser);
        when(supabaseAuthPort.signIn(any(SignInCommand.class))).thenReturn(session);
        when(userProfileRepositoryPort.save(any(UserProfile.class))).thenReturn(expectedSessionProfile);

        // WHEN
        AuthSession authSession = authenticationService.signUp(command);

        // THEN
        assertThat(authSession).isNotNull();
        assertThat(authSession.profile()).isEqualTo(expectedSessionProfile);
        assertThat(authSession.tokens()).isEqualTo(tokens);
        
        verify(supabaseAuthPort).createUser(command);
        verify(supabaseAuthPort).signIn(signInCommandCaptor.capture());
        verify(userProfileRepositoryPort).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(supabaseAuthPort, userProfileRepositoryPort);

        // Verify the SignInCommand was created with correct credentials
        SignInCommand capturedSignIn = signInCommandCaptor.getValue();
        assertThat(capturedSignIn.email()).isEqualTo(EMAIL);
        assertThat(capturedSignIn.password()).isEqualTo(PASSWORD);

        // CRITICAL: Verify the UserProfile was created from session.user() (NOT createdUser)
        // This kills the mutation that replaces the != null check with false
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id())
                .as("Should use session.user() ID when it is not null")
                .isEqualTo(USER_ID);  // From sessionUser, NOT createdUserId
        assertThat(capturedProfile.email())
                .as("Should use session.user() email when it is not null")
                .isEqualTo(EMAIL);  // From sessionUser, NOT "created@example.com"
        assertThat(capturedProfile.role())
                .as("Should use session.user() role when it is not null")
                .isEqualTo(Role.ADMIN);  // From sessionUser, NOT Role.MANAGER
    }

    @Test
    void GIVEN_signUpWithNullSessionUser_WHEN_signUp_THEN_usesCreatedUserForProfile() {
        // GIVEN
        SignUpCommand command = new SignUpCommand(FIRM_ID, EMAIL, PASSWORD, Role.ADMIN);
        
        // Create a createdUser with specific attributes different from the session user
        UUID createdUserId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        SupabaseUser createdUser = new SupabaseUser(createdUserId, FIRM_ID, "created@example.com", Role.EMPLOYEE);
        
        // Session returns null user - should fall back to createdUser
        SupabaseAuthSession sessionWithNullUser = new SupabaseAuthSession(null, tokens);
        
        UserProfile expectedCreatedProfile = new UserProfile(createdUserId, FIRM_ID, "created@example.com", Role.EMPLOYEE);
        
        when(supabaseAuthPort.createUser(command)).thenReturn(createdUser);
        when(supabaseAuthPort.signIn(any(SignInCommand.class))).thenReturn(sessionWithNullUser);
        when(userProfileRepositoryPort.save(any(UserProfile.class))).thenReturn(expectedCreatedProfile);

        // WHEN
        AuthSession session = authenticationService.signUp(command);

        // THEN
        assertThat(session).isNotNull();
        assertThat(session.profile()).isEqualTo(expectedCreatedProfile);
        assertThat(session.tokens()).isEqualTo(tokens);
        
        verify(supabaseAuthPort).createUser(command);
        verify(supabaseAuthPort).signIn(any(SignInCommand.class));
        verify(userProfileRepositoryPort).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(supabaseAuthPort, userProfileRepositoryPort);

        // CRITICAL: Verify the profile was created from the createdUser (not session.user())
        // This kills the mutation that replaces the != null check with false
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id())
                .as("Should use createdUser ID when session.user() is null")
                .isEqualTo(createdUserId);
        assertThat(capturedProfile.email())
                .as("Should use createdUser email when session.user() is null")
                .isEqualTo("created@example.com");
        assertThat(capturedProfile.role())
                .as("Should use createdUser role when session.user() is null")
                .isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void GIVEN_validSignInCommand_WHEN_signIn_THEN_authenticatesAndReturnsAuthSession() {
        // GIVEN
        SignInCommand command = new SignInCommand(EMAIL, PASSWORD);
        when(supabaseAuthPort.signIn(command)).thenReturn(supabaseAuthSession);
        when(userProfileRepositoryPort.save(any(UserProfile.class))).thenReturn(expectedProfile);

        // WHEN
        AuthSession session = authenticationService.signIn(command);

        // THEN
        assertThat(session).isNotNull();
        assertThat(session.profile()).isEqualTo(expectedProfile);
        assertThat(session.tokens()).isEqualTo(tokens);
        
        verify(supabaseAuthPort).signIn(command);
        verify(userProfileRepositoryPort).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(supabaseAuthPort, userProfileRepositoryPort);

        // Verify the UserProfile was created with correct data from session user
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id()).isEqualTo(USER_ID);
        assertThat(capturedProfile.firmId()).isEqualTo(FIRM_ID);
        assertThat(capturedProfile.email()).isEqualTo(EMAIL);
        assertThat(capturedProfile.role()).isEqualTo(Role.ADMIN);
    }
}
