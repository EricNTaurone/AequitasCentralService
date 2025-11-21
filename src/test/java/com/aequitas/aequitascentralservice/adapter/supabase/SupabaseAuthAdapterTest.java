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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.aequitas.aequitascentralservice.config.SupabaseProperties;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
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

    SupabaseProperties goodProps;

    @BeforeEach
    void setUp() {
        goodProps = new SupabaseProperties("https://x.supabase.co", "service-key", null);
    }

    @Test
    void GIVEN_missing_service_key_WHEN_construct_THEN_throws() {
        final SupabaseProperties p = new SupabaseProperties("https://x", "", null);
        assertThatThrownBy(() -> new SupabaseAuthAdapter(restClient, p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase service key must be configured");
    }

    @Test
    void GIVEN_missing_url_WHEN_construct_THEN_throws() {
        final SupabaseProperties p = new SupabaseProperties("", "key", null);
        assertThatThrownBy(() -> new SupabaseAuthAdapter(restClient, p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase base URL must be configured");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_valid_signup_WHEN_createUser_THEN_maps_to_domain_user() throws Exception {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("a@b.com").password("pw").role(Role.MANAGER).build();

        final Object userResp = createUserResponse("11111111-1111-1111-1111-111111111111", "a@b.com",
                Map.of(), Map.of("firm_id", firmId.toString(), "role", "manager"), null, null);

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(userResp).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        // WHEN
        final SupabaseUser user = adapter.createUser(cmd);

        // THEN
        assertThat(user.email()).isEqualTo("a@b.com");
        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_signin_successful_WHEN_signIn_THEN_returns_session_with_tokens_and_user() throws Exception {
        // GIVEN
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        final UUID firmId = UUID.randomUUID();
        final Object userResp = createUserResponse("22222222-2222-2222-2222-222222222222", "x@x.com",
                Map.of(), Map.of("firm_id", firmId.toString(), "role", "employee"), null, null);

        final Object signInResp = createSignInResponse("access-1", "refresh-1", 3600L, "bearer", userResp);

        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(signInResp).when(responseSpec).body((Class<Object>) signInClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        // WHEN
        final var session = adapter.signIn(cmd);

        // THEN
        assertThat(session).isNotNull();
        final SupabaseUser user = session.user();
        final AuthTokens tokens = session.tokens();
        assertThat(user.email()).isEqualTo("x@x.com");
        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.EMPLOYEE);
        assertThat(tokens.accessToken()).isEqualTo("access-1");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-1");
        assertThat(tokens.expiresInSeconds()).isEqualTo(3600L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_restclient_throws_WHEN_createUser_translates_exception() {
        final SignUpCommand cmd = SignUpCommand.builder().firmId(UUID.randomUUID()).email("a@b.com").password("pw").role(Role.ADMIN).build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doThrow(new RestClientResponseException("bad", 400, "BAD", null, null, null)).when(responseSpec).body(any(Class.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.createUser(cmd)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create Supabase user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_firmId_in_appMetadata_WHEN_createUser_THEN_resolvesFromAppMetadata() throws Exception {
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.EMPLOYEE).build();

        final Object userResp = createUserResponse("33333333-3333-3333-3333-333333333333", "test@test.com",
                Map.of("firm_id", firmId.toString()), Map.of(), "employee", null);

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(userResp).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);
        final SupabaseUser user = adapter.createUser(cmd);

        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_firmId_direct_field_WHEN_createUser_THEN_resolvesFromDirectField() throws Exception {
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.ADMIN).build();

        final Object userResp = createUserResponse("44444444-4444-4444-4444-444444444444", "test@test.com",
                Map.of(), Map.of(), "admin", firmId.toString());

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(userResp).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);
        final SupabaseUser user = adapter.createUser(cmd);

        assertThat(user.firmId()).isEqualTo(firmId);
        assertThat(user.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_missing_firmId_WHEN_createUser_THEN_throws() throws Exception {
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.ADMIN).build();

        final Object userResp = createUserResponse("55555555-5555-5555-5555-555555555555", "test@test.com",
                Map.of(), Map.of(), "admin", null);

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(userResp).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.createUser(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase user is missing firm_id metadata");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_role_in_appMetadata_WHEN_createUser_THEN_resolvesFromAppMetadata() throws Exception {
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.MANAGER).build();

        final Object userResp = createUserResponse("66666666-6666-6666-6666-666666666666", "test@test.com",
                Map.of("role", "manager"), Map.of("firm_id", firmId.toString()), null, null);

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(userResp).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);
        final SupabaseUser user = adapter.createUser(cmd);

        assertThat(user.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_response_WHEN_createUser_THEN_throws() throws Exception {
        final UUID firmId = UUID.randomUUID();
        final SignUpCommand cmd = SignUpCommand.builder().firmId(firmId).email("test@test.com").password("pw").role(Role.ADMIN).build();

        final Class<?> userRespClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseUserResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(null).when(responseSpec).body((Class<Object>) userRespClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.createUser(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase create user returned no payload");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_user_in_response_WHEN_signIn_THEN_throws() throws Exception {
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        final Object signInResp = createSignInResponse("access-1", "refresh-1", 3600L, "bearer", null);

        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(signInResp).when(responseSpec).body((Class<Object>) signInClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase sign-in did not return a user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_null_signIn_response_WHEN_signIn_THEN_throws() throws Exception {
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        final Class<?> signInClass = Class.forName("com.aequitas.aequitascentralservice.adapter.supabase.SupabaseAuthAdapter$SupabaseSignInResponse");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(null).when(responseSpec).body((Class<Object>) signInClass);

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Supabase sign-in did not return a user");
    }

    @Test
    @SuppressWarnings("unchecked")
    void GIVEN_signIn_throws_WHEN_signIn_THEN_translates_exception() {
        final SignInCommand cmd = SignInCommand.builder().email("x@x.com").password("pw").build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doThrow(new RestClientResponseException("unauthorized", 401, "UNAUTHORIZED", null, null, null)).when(responseSpec).body(any(Class.class));

        final SupabaseAuthAdapter adapter = new SupabaseAuthAdapter(restClient, goodProps);

        assertThatThrownBy(() -> adapter.signIn(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to sign in with Supabase");
    }

    // --- reflection helpers to instantiate private nested records ---
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

