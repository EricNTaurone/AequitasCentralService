package com.aequitas.aequitascentralservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class SupabasePropertiesTest {

    private static final String TEST_URL = "https://test.supabase.co";
    private static final String TEST_SERVICE_KEY = "test-service-key-12345";
    private static final String TEST_JWK_SET_URI = "https://test.supabase.co/.well-known/jwks.json";

    @Test
    void GIVEN_ValidParameters_WHEN_SupabasePropertiesCreated_THEN_AllFieldsAreSet() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(TEST_JWK_SET_URI);

        // WHEN
        final SupabaseProperties result = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertEquals(TEST_URL, result.url(), "URL should match expected value");
        assertEquals(TEST_SERVICE_KEY, result.serviceKey(), "Service key should match expected value");
        assertNotNull(result.auth(), "Auth should not be null");
        assertEquals(TEST_JWK_SET_URI, result.auth().jwkSetUri(), "JWK Set URI should match expected value");
    }

    @Test
    void GIVEN_NullAuth_WHEN_SupabasePropertiesCreated_THEN_AuthIsInitializedWithNull() {
        // GIVEN
        // auth is null

        // WHEN
        final SupabaseProperties result = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, null);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertEquals(TEST_URL, result.url(), "URL should match expected value");
        assertEquals(TEST_SERVICE_KEY, result.serviceKey(), "Service key should match expected value");
        assertNotNull(result.auth(), "Auth should not be null, should be initialized");
        assertNull(result.auth().jwkSetUri(), "JWK Set URI should be null");
    }

    @Test
    void GIVEN_NullUrl_WHEN_SupabasePropertiesCreated_THEN_UrlIsNull() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(TEST_JWK_SET_URI);

        // WHEN
        final SupabaseProperties result = new SupabaseProperties(null, TEST_SERVICE_KEY, auth);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertNull(result.url(), "URL should be null");
        assertEquals(TEST_SERVICE_KEY, result.serviceKey(), "Service key should match expected value");
        assertNotNull(result.auth(), "Auth should not be null");
    }

    @Test
    void GIVEN_NullServiceKey_WHEN_SupabasePropertiesCreated_THEN_ServiceKeyIsNull() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(TEST_JWK_SET_URI);

        // WHEN
        final SupabaseProperties result = new SupabaseProperties(TEST_URL, null, auth);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertEquals(TEST_URL, result.url(), "URL should match expected value");
        assertNull(result.serviceKey(), "Service key should be null");
        assertNotNull(result.auth(), "Auth should not be null");
    }

    @Test
    void GIVEN_AllNullParameters_WHEN_SupabasePropertiesCreated_THEN_OnlyAuthIsInitialized() {
        // GIVEN
        // all parameters are null

        // WHEN
        final SupabaseProperties result = new SupabaseProperties(null, null, null);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertNull(result.url(), "URL should be null");
        assertNull(result.serviceKey(), "Service key should be null");
        assertNotNull(result.auth(), "Auth should not be null, should be initialized");
        assertNull(result.auth().jwkSetUri(), "JWK Set URI should be null");
    }

    @Test
    void GIVEN_ValidAuthParameters_WHEN_AuthCreated_THEN_JwkSetUriIsSet() {
        // GIVEN
        // TEST_JWK_SET_URI constant

        // WHEN
        final SupabaseProperties.Auth result = new SupabaseProperties.Auth(TEST_JWK_SET_URI);

        // THEN
        assertNotNull(result, "Auth should not be null");
        assertEquals(TEST_JWK_SET_URI, result.jwkSetUri(), "JWK Set URI should match expected value");
    }

    @Test
    void GIVEN_NullJwkSetUri_WHEN_AuthCreated_THEN_JwkSetUriIsNull() {
        // GIVEN
        // jwkSetUri is null

        // WHEN
        final SupabaseProperties.Auth result = new SupabaseProperties.Auth(null);

        // THEN
        assertNotNull(result, "Auth should not be null");
        assertNull(result.jwkSetUri(), "JWK Set URI should be null");
    }

    @Test
    void GIVEN_TwoIdenticalSupabaseProperties_WHEN_Compared_THEN_AreEqual() {
        // GIVEN
        final SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(TEST_JWK_SET_URI);
        final SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth(TEST_JWK_SET_URI);
        final SupabaseProperties props1 = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth1);
        final SupabaseProperties props2 = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth2);

        // WHEN
        final int hashCode1 = props1.hashCode();
        final int hashCode2 = props2.hashCode();

        // THEN
        assertEquals(props1, props2, "Identical SupabaseProperties should be equal");
        assertEquals(hashCode1, hashCode2, "Hash codes of identical SupabaseProperties should be equal");
    }

    @Test
    void GIVEN_TwoDifferentSupabaseProperties_WHEN_Compared_THEN_AreNotEqual() {
        // GIVEN
        final SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(TEST_JWK_SET_URI);
        final SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth("different-uri");
        final SupabaseProperties props1 = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth1);
        final SupabaseProperties props2 = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth2);

        // WHEN & THEN
        assertNotNull(props1, "First properties should not be null");
        assertNotNull(props2, "Second properties should not be null");
        assertEquals(false, props1.equals(props2), "Different SupabaseProperties should not be equal");
    }

    @Test
    void GIVEN_SupabaseProperties_WHEN_ToStringCalled_THEN_ReturnsNonNullString() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(TEST_JWK_SET_URI);
        final SupabaseProperties props = new SupabaseProperties(TEST_URL, TEST_SERVICE_KEY, auth);

        // WHEN
        final String result = props.toString();

        // THEN
        assertNotNull(result, "toString should not return null");
        assertEquals(false, result.isEmpty(), "toString should not return empty string");
    }

    @Test
    void GIVEN_Auth_WHEN_ToStringCalled_THEN_ReturnsNonNullString() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(TEST_JWK_SET_URI);

        // WHEN
        final String result = auth.toString();

        // THEN
        assertNotNull(result, "toString should not return null");
        assertEquals(false, result.isEmpty(), "toString should not return empty string");
    }

    @Test
    void GIVEN_EmptyStrings_WHEN_SupabasePropertiesCreated_THEN_EmptyStringsArePreserved() {
        // GIVEN
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth("");

        // WHEN
        final SupabaseProperties result = new SupabaseProperties("", "", auth);

        // THEN
        assertNotNull(result, "SupabaseProperties should not be null");
        assertEquals("", result.url(), "URL should be empty string");
        assertEquals("", result.serviceKey(), "Service key should be empty string");
        assertNotNull(result.auth(), "Auth should not be null");
        assertEquals("", result.auth().jwkSetUri(), "JWK Set URI should be empty string");
    }
}
