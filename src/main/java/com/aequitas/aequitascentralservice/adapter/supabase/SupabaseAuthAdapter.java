package com.aequitas.aequitascentralservice.adapter.supabase;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.aequitas.aequitascentralservice.app.port.outbound.SupabaseAuthPort;
import com.aequitas.aequitascentralservice.config.SupabaseProperties;
import com.aequitas.aequitascentralservice.constants.SupabaseConstants;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.SupabaseAuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
import com.aequitas.aequitascentralservice.domain.value.AuthTokens;
import com.aequitas.aequitascentralservice.domain.value.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HTTP adapter that calls Supabase Auth endpoints for user creation and
 * password authentication.
 */
@Component
public class SupabaseAuthAdapter implements SupabaseAuthPort {

    private final RestClient restClient;
    private final SupabaseProperties properties;

    public SupabaseAuthAdapter(final RestClient supabaseRestClient, final SupabaseProperties properties) {
        if (!StringUtils.hasText(properties.serviceKey())) {
            throw new IllegalStateException("Supabase service key must be configured");
        }
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("Supabase base URL must be configured");
        }
        
        this.restClient = supabaseRestClient;
        this.properties = properties;
    }

    @Override
    public SupabaseUser createUser(final SignUpCommand command) {
        try {
            final SupabaseUserResponse response = restClient
                    .post()
                    .uri(String.format(SupabaseConstants.SIGNUP_URI, properties.url()))
                    .header(SupabaseConstants.API_KEY_HEADER, properties.serviceKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AdminCreateUserRequest(command.email(), command.password(), command.firmId(), command.role()))
                    .retrieve()
                    .body(SupabaseUserResponse.class);
            if (response == null) {
                throw new IllegalStateException("Supabase create user returned no payload");
            }
            return toDomainUser(response, command.role());
        } catch (RestClientResponseException ex) {
            throw translate("create Supabase user", ex);
        }
    }

    @Override
    public SupabaseAuthSession signIn(final SignInCommand command) {
        try {
            final SupabaseSignInResponse response = restClient
                    .post()
                    .uri(String.format(SupabaseConstants.SIGNIN_URI, properties.url()))
                    .header(SupabaseConstants.API_KEY_HEADER, properties.serviceKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SignInRequest(command.email(), command.password()))
                    .retrieve()
                    .body(SupabaseSignInResponse.class);
            if (response == null || response.user() == null) {
                throw new IllegalStateException("Supabase sign-in did not return a user");
            }
            final SupabaseUser user = toDomainUser(response.user(), null);
            final AuthTokens tokens
                    = new AuthTokens(response.accessToken(), response.refreshToken(), response.expiresIn(), response.tokenType());
            return new SupabaseAuthSession(user, tokens);
        } catch (RestClientResponseException ex) {
            throw translate("sign in with Supabase", ex);
        }
    }

    private SupabaseUser toDomainUser(final SupabaseUserResponse response, final Role fallbackRole) {
        final UUID id = UUID.fromString(response.id());
        final UUID firmId = resolveFirmId(response);
        final Role role = resolveRole(response, fallbackRole);
        return new SupabaseUser(id, firmId, response.email(), role);
    }

    private UUID resolveFirmId(final SupabaseUserResponse response) {
        final UUID fromUserMetadata = parseUuid(response.userMetadata(), "firm_id");
        if (fromUserMetadata != null) {
            return fromUserMetadata;
        }
        final UUID fromAppMetadata = parseUuid(response.appMetadata(), "firm_id");
        if (fromAppMetadata != null) {
            return fromAppMetadata;
        }
        final String direct = response.firmId();
        if (StringUtils.hasText(direct)) {
            return UUID.fromString(direct);
        }
        throw new IllegalStateException("Supabase user is missing firm_id metadata");
    }

    private Role resolveRole(final SupabaseUserResponse response, final Role fallbackRole) {
        final String roleFromUserMetadata = parseString(response.userMetadata(), "role");
        if (StringUtils.hasText(roleFromUserMetadata)) {
            return Role.valueOf(roleFromUserMetadata.trim().toUpperCase());
        }
        final String roleFromAppMetadata = parseString(response.appMetadata(), "role");
        if (StringUtils.hasText(roleFromAppMetadata)) {
            return Role.valueOf(roleFromAppMetadata.trim().toUpperCase());
        }
        if (StringUtils.hasText(response.role())) {
            try {
                return Role.valueOf(response.role().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // fall through to fallback
            }
        }
        if (fallbackRole != null) {
            return fallbackRole;
        }
        throw new IllegalStateException("Supabase user is missing role metadata");
    }

    private UUID parseUuid(final Map<String, Object> map, final String key) {
        if (map == null) {
            return null;
        }
        final Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return UUID.fromString(String.valueOf(value));
    }

    private String parseString(final Map<String, Object> map, final String key) {
        if (map == null) {
            return null;
        }
        final Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private IllegalStateException translate(final String action, final RestClientResponseException ex) {
        final HttpStatusCode status = ex.getStatusCode();
        final String message = ex.getResponseBodyAsString();
        return new IllegalStateException(
                "Failed to %s: %s %s".formatted(action, status, StringUtils.hasText(message) ? message : ""), ex);
    }

    private record AdminCreateUserRequest(
            String email,
            String password,
            @JsonProperty("email_confirm") boolean emailConfirm,
            @JsonProperty("user_metadata") Map<String, Object> userMetadata) {

        private AdminCreateUserRequest(final String email, final String password, final UUID firmId, final Role role) {
            this(email, password, true, Map.of("firm_id", firmId.toString(), "role", role.name()));
        }
    }

    private record SignInRequest(String email, String password) {

    }

    private record SupabaseUserResponse(
            String id,
            String email,
            @JsonProperty("app_metadata") Map<String, Object> appMetadata,
            @JsonProperty("user_metadata") Map<String, Object> userMetadata,
            String role,
            @JsonProperty("firm_id") String firmId) {

    }

    private record SupabaseSignInResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("token_type") String tokenType,
            SupabaseUserResponse user) {

    }
}
