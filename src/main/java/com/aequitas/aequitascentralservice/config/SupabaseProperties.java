package com.aequitas.aequitascentralservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindable properties for Supabase integration.
 *
 * @param url base Supabase project URL (e.g., https://xyz.supabase.co).
 * @param serviceKey service role key used for admin operations.
 * @param auth nested authentication properties.
 */
@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(String url, String serviceKey, Auth auth) {

    public SupabaseProperties {
        auth = auth == null ? new Auth(null) : auth;
    }

    /**
     * Properties for Supabase auth.
     *
     * @param jwkSetUri URI to fetch the Supabase JWKS used for access token verification.
     */
    public record Auth(String jwkSetUri) {}
}
