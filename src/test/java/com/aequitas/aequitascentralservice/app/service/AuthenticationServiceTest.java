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
    private static final UUID AUTH_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID DOMAIN_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
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
        // SupabaseUser: (authId, domainId, firmId, email, role)
        supabaseUser = new SupabaseUser(AUTH_ID, DOMAIN_ID, FIRM_ID, EMAIL, Role.ADMIN);
        supabaseAuthSession = new SupabaseAuthSession(supabaseUser, tokens);
        // UserProfile: (id=domainId, authenticationId=authId, firmId, email, role)
        expectedProfile = new UserProfile(DOMAIN_ID, AUTH_ID, FIRM_ID, EMAIL, Role.ADMIN);
    }

    @Test
    void GIVEN_validSignUpCommand_WHEN_signUp_THEN_createsUserAndReturnsAuthSession() {
        // GIVEN
        SignUpCommand command = new SignUpCommand(FIRM_ID, EMAIL, PASSWORD, Role.ADMIN);
        
        // Create a createdUser with one set of attributes (different from session user)
        UUID createdAuthId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        UUID createdDomainId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        SupabaseUser createdUser = new SupabaseUser(createdAuthId, createdDomainId, FIRM_ID, "created@example.com", Role.MANAGER);
        
        // Session user has DIFFERENT attributes - this is what should be used when session.user() != null
        UUID sessionAuthId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        UUID sessionDomainId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SupabaseUser sessionUser = new SupabaseUser(sessionAuthId, sessionDomainId, FIRM_ID, EMAIL, Role.ADMIN);
        SupabaseAuthSession session = new SupabaseAuthSession(sessionUser, tokens);
        
        // Expected profile uses sessionUser's domainId as id, and sessionUser's authId as authenticationId
        UserProfile expectedSessionProfile = new UserProfile(sessionDomainId, sessionAuthId, FIRM_ID, EMAIL, Role.ADMIN);
        
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
        // UserProfile.id = SupabaseUser.domainId, UserProfile.authenticationId = SupabaseUser.id
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id())
                .as("Should use session.user().domainId() as profile id when session.user() is not null")
                .isEqualTo(sessionDomainId);  // From sessionUser.domainId(), NOT createdDomainId
        assertThat(capturedProfile.authenticationId())
                .as("Should use session.user().id() as authenticationId when session.user() is not null")
                .isEqualTo(sessionAuthId);  // From sessionUser.id(), NOT createdAuthId
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
        
        // Create a createdUser with specific attributes
        UUID createdAuthId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdDomainId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        SupabaseUser createdUser = new SupabaseUser(createdAuthId, createdDomainId, FIRM_ID, "created@example.com", Role.EMPLOYEE);
        
        // Session returns null user - should fall back to createdUser
        SupabaseAuthSession sessionWithNullUser = new SupabaseAuthSession(null, tokens);
        
        // Expected profile uses createdUser's domainId as id, and createdUser's authId as authenticationId
        UserProfile expectedCreatedProfile = new UserProfile(createdDomainId, createdAuthId, FIRM_ID, "created@example.com", Role.EMPLOYEE);
        
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
        // UserProfile.id = SupabaseUser.domainId, UserProfile.authenticationId = SupabaseUser.id
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id())
                .as("Should use createdUser.domainId() as profile id when session.user() is null")
                .isEqualTo(createdDomainId);
        assertThat(capturedProfile.authenticationId())
                .as("Should use createdUser.id() as authenticationId when session.user() is null")
                .isEqualTo(createdAuthId);
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

        // Verify the UserProfile was created with correct mapping from session user
        // UserProfile.id = SupabaseUser.domainId, UserProfile.authenticationId = SupabaseUser.id
        UserProfile capturedProfile = userProfileCaptor.getValue();
        assertThat(capturedProfile.id())
                .as("UserProfile.id should be SupabaseUser.domainId()")
                .isEqualTo(DOMAIN_ID);
        assertThat(capturedProfile.authenticationId())
                .as("UserProfile.authenticationId should be SupabaseUser.id()")
                .isEqualTo(AUTH_ID);
        assertThat(capturedProfile.firmId()).isEqualTo(FIRM_ID);
        assertThat(capturedProfile.email()).isEqualTo(EMAIL);
        assertThat(capturedProfile.role()).isEqualTo(Role.ADMIN);
    }
}
