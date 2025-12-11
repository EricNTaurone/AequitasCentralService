package com.aequitas.aequitascentralservice.adapter.supabase;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.aequitas.aequitascentralservice.app.service.UserProfileService;
import com.aequitas.aequitascentralservice.config.SupabaseProperties;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.AuthTokens;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupabaseAuthAdapterTest {

    @Mock
    RestClient restClient;

    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    RestClient.RequestBodySpec requestBodySpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @Mock
    UserProfileService userProfileService;

    SupabaseProperties goodProps;

    @BeforeEach
    void setUp() {
        goodProps = new SupabaseProperties("https://x.supabase.co", "service-key", null);
    }

    @Test
    void GIVEN_missing_service_key_WHEN_construct_THEN_throws() {
        final SupabaseProperties p = new SupabaseProperties("https://x", "", null);
        assertThatThrownBy(() -> new SupabaseAuthAdapter(restClient, p, userProfileService))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase service key must be configured");
    }

    @Test
    void GIVEN_missing_url_WHEN_construct_THEN_throws() {
        final SupabaseProperties p = new SupabaseProperties("", "key", null);
        assertThatThrownBy(() -> new SupabaseAuthAdapter(restClient, p, userProfileService))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase base URL must be configured");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_valid_signup_WHEN_createUser_THEN_maps_to_domain_user() throws Exception {
        // GIVEN
        final UUID authId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID profileId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("a@b.com").password("pw").role(Role.MANAGER).build();

        final Object userResp = createUserResponse(authId.toString(), "a@b.com", Map.of(), Map.of(), null, null);
        final UserProfile savedProfile = UserProfile.builder()
                .id(profileId)
                .authenticationId(authId)
                .firmId(firmId)
                .email("a@b.com")
                .role(Role.MANAGER)
                .build();

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        stubRestClientPost(userRespClass, userResp);
        doReturn(savedProfile).when(userProfileService).createUserProfile(any(UserProfile.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN
        final SupabaseUser user = adapter.createUser(cmd);

        // THEN
        assertThat(user.id()).isEqualTo(authId);
        assertThat(user.domainId()).isEqualTo(profileId);
        assertThat(user.email()).isEqualTo("a@b.com");
        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.MANAGER);
        verify(userProfileService).createUserProfile(any(UserProfile.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_signin_successful_WHEN_signIn_THEN_returns_session_with_tokens_and_user() throws Exception {
        // GIVEN
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();
        final UUID authId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final UUID profileId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final Object userResp = createUserResponse(authId.toString(), "x@x.com", Map.of(), Map.of(), null, null);
        final Object signInResp = createSignInResponse("access-1", "refresh-1", 3600L, "bearer", userResp);
        final UserProfile existingProfile = UserProfile.builder()
                .id(profileId)
                .authenticationId(authId)
                .firmId(firmId)
                .email("x@x.com")
                .role(Role.EMPLOYEE)
                .build();

        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        stubRestClientPost(signInClass, signInResp);
        doReturn(existingProfile).when(userProfileService).findByAuthenticationId(authId);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN
        final var session = adapter.signIn(cmd);

        // THEN
        assertThat(session).isNotNull();
        final SupabaseUser user = session.user();
        final AuthTokens tokens = session.tokens();
        assertThat(user.id()).isEqualTo(authId);
        assertThat(user.domainId()).isEqualTo(profileId);
        assertThat(user.email()).isEqualTo("x@x.com");
        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.EMPLOYEE);
        assertThat(tokens.accessToken()).isEqualTo("access-1");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-1");
        assertThat(tokens.expiresInSeconds()).isEqualTo(3600L);
        assertThat(tokens.tokenType()).isEqualTo("bearer");
        verify(userProfileService).findByAuthenticationId(authId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_restclient_throws_WHEN_createUser_THEN_translates_exception() {
        // GIVEN
        final SignUpCommand cmd = SignUpCommand.builder().firmId(UUID.randomUUID()).email("a@b.com").password("pw").role(Role.ADMIN).build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doThrow(new RestClientResponseException("bad", 400, "BAD", null, null, null)).when(responseSpec).body(any(Class.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.createUser(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create Supabase user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_response_WHEN_createUser_THEN_throws() throws Exception {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.ADMIN).build();

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        stubRestClientPost(userRespClass, null);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.createUser(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase create user returned no payload");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_user_in_response_WHEN_signIn_THEN_throws() throws Exception {
        // GIVEN
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        final Object signInResp = createSignInResponse("access-1", "refresh-1", 3600L, "bearer", null);
        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        stubRestClientPost(signInClass, signInResp);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase sign-in did not return a user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_signIn_response_WHEN_signIn_THEN_throws() throws Exception {
        // GIVEN
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        stubRestClientPost(signInClass, null);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase sign-in did not return a user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_signIn_throws_WHEN_signIn_THEN_translates_exception() {
        // GIVEN
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doThrow(new RestClientResponseException("unauthorized", 401, "UNAUTHORIZED", null, null, null)).when(responseSpec).body(any(Class.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to sign in with Supabase");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_restclient_throws_with_empty_body_WHEN_createUser_THEN_translates_with_empty_message() {
        // GIVEN - exception with no response body to cover the StringUtils.hasText branch
        final SignUpCommand cmd = SignUpCommand.builder().firmId(UUID.randomUUID()).email("a@b.com").password("pw").role(Role.ADMIN).build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        // Create exception with empty body
        doThrow(new RestClientResponseException("", 500, "INTERNAL", null, new byte[0], null)).when(responseSpec).body(any(Class.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps, userProfileService);

        // WHEN/THEN
        assertThatThrownBy(() -> adapter.createUser(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create Supabase user");
    }

    // --- helper methods ---

    @SuppressWarnings("unchecked")
    private void stubRestClientPost(final Class<?> responseClass, final Object response) {
        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(response).when(responseSpec).body((Class<Object>) responseClass);
    }

    private Object createUserResponse(final String id, final String email, final Map<String, Object> appMetadata,
            final Map<String, Object> userMetadata, final String role, final String firmId) throws Exception {
        final Class<?> cls = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");
        final Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class, Map.class, Map.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(id, email, appMetadata, userMetadata, role, firmId);
    }

    private Object createSignInResponse(final String access, final String refresh, final long expires, final String tokenType,
            final Object userResp) throws Exception {
        final Class<?> cls = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");
        final Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class, long.class, String.class, Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse"));
        ctor.setAccessible(true);
        return ctor.newInstance(access, refresh, expires, tokenType, userResp);
    }
}

