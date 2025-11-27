package com.aequitas.aequitascentralservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Wires the REST client used to call Supabase Admin and Auth endpoints.
 */
@Configuration
@EnableConfigurationProperties(SupabaseProperties.class)
public class SupabaseConfig {

    @Bean
    public RestClient supabaseRestClient(final SupabaseProperties properties) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("supabase.url must be configured");
        }
        if (!StringUtils.hasText(properties.serviceKey())) {
            throw new IllegalStateException("supabase.service-key must be configured");
        }
        final String baseUrl =
                UriComponentsBuilder.fromUriString(properties.url())
                        .path("/auth/v1")
                        .build()
                        .toString();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", properties.serviceKey())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.serviceKey())
                .build();
    }
}
