package com.aequitas.aequitascentralservice.config;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenApiConfigTest {

    private static final String EXPECTED_SECURITY_SCHEME_NAME = "bearer-jwt";
    private static final String EXPECTED_TITLE = "Aequitas Central Service API";
    private static final String EXPECTED_VERSION = "v1";
    private static final String EXPECTED_DESCRIPTION = "Time entry and approval API surface.";
    private static final String EXPECTED_SCHEME = "bearer";
    private static final String EXPECTED_BEARER_FORMAT = "JWT";

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_ReturnNonNullOpenAPI() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();

        // THEN
        assertNotNull(result, "OpenAPI should not be null");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_InfoIsConfiguredCorrectly() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final Info info = result.getInfo();

        // THEN
        assertNotNull(info, "Info should not be null");
        assertEquals(EXPECTED_TITLE, info.getTitle(), "Title should match expected value");
        assertEquals(EXPECTED_VERSION, info.getVersion(), "Version should match expected value");
        assertEquals(EXPECTED_DESCRIPTION, info.getDescription(), "Description should match expected value");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_SecurityRequirementIsConfigured() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final List<SecurityRequirement> securityRequirements = result.getSecurity();

        // THEN
        assertNotNull(securityRequirements, "Security requirements should not be null");
        assertFalse(securityRequirements.isEmpty(), "Security requirements should not be empty");
        assertEquals(1, securityRequirements.size(), "Should have exactly one security requirement");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_SecurityRequirementContainsBearerJwt() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final SecurityRequirement securityRequirement = result.getSecurity().get(0);

        // THEN
        assertNotNull(securityRequirement, "Security requirement should not be null");
        assertTrue(securityRequirement.containsKey(EXPECTED_SECURITY_SCHEME_NAME),
                "Security requirement should contain bearer-jwt key");
        assertNotNull(securityRequirement.get(EXPECTED_SECURITY_SCHEME_NAME),
                "Security requirement list for bearer-jwt should not be null");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_ComponentsAreConfigured() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final Components components = result.getComponents();

        // THEN
        assertNotNull(components, "Components should not be null");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_SecuritySchemeIsConfigured() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final Map<String, SecurityScheme> securitySchemes = result.getComponents().getSecuritySchemes();

        // THEN
        assertNotNull(securitySchemes, "Security schemes should not be null");
        assertFalse(securitySchemes.isEmpty(), "Security schemes should not be empty");
        assertTrue(securitySchemes.containsKey(EXPECTED_SECURITY_SCHEME_NAME),
                "Security schemes should contain bearer-jwt");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_SecuritySchemeHasCorrectProperties() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();
        final SecurityScheme securityScheme = result.getComponents()
                .getSecuritySchemes()
                .get(EXPECTED_SECURITY_SCHEME_NAME);

        // THEN
        assertNotNull(securityScheme, "Security scheme should not be null");
        assertEquals(EXPECTED_SECURITY_SCHEME_NAME, securityScheme.getName(),
                "Security scheme name should match expected value");
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType(),
                "Security scheme type should be HTTP");
        assertEquals(EXPECTED_SCHEME, securityScheme.getScheme(),
                "Security scheme should be bearer");
        assertEquals(EXPECTED_BEARER_FORMAT, securityScheme.getBearerFormat(),
                "Bearer format should be JWT");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_MultipleCallsToOpenAPI_THEN_EachReturnsNewInstance() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result1 = openApiConfig.openAPI();
        final OpenAPI result2 = openApiConfig.openAPI();

        // THEN
        assertNotNull(result1, "First OpenAPI should not be null");
        assertNotNull(result2, "Second OpenAPI should not be null");
        assertFalse(result1 == result2,
                "Each call should return a new instance (not Spring-managed singleton in this test)");
    }

    @Test
    void GIVEN_OpenApiConfig_WHEN_OpenAPICalled_THEN_AllRequiredFieldsArePresent() {
        // GIVEN
        // openApiConfig is initialized in setUp

        // WHEN
        final OpenAPI result = openApiConfig.openAPI();

        // THEN
        assertNotNull(result.getInfo(), "Info should be present");
        assertNotNull(result.getInfo().getTitle(), "Title should be present");
        assertNotNull(result.getInfo().getVersion(), "Version should be present");
        assertNotNull(result.getInfo().getDescription(), "Description should be present");
        assertNotNull(result.getSecurity(), "Security requirements should be present");
        assertNotNull(result.getComponents(), "Components should be present");
        assertNotNull(result.getComponents().getSecuritySchemes(), "Security schemes should be present");
    }
}
