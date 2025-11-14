package com.aequitas.aequitascentralservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configures the OpenAPI document and security components.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Builds the OpenAPI descriptor for Swagger UI.
   *
   * @return configured descriptor.
   */
  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "bearer-jwt";
    return new OpenAPI()
        .info(
            new Info()
                .title("Aequitas Central Service API")
                .version("v1")
                .description("Time entry and approval API surface."))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
