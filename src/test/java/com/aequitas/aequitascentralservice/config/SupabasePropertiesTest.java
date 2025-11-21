package com.aequitas.aequitascentralservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Production-grade JUnit 5 tests for SupabaseProperties.
 * Targets: 100% Line Coverage, 100% Branch Coverage, 100% Mutation Score.
 */
class SupabasePropertiesTest {

    private static final String VALID_URL = "https://xyz.supabase.co";
    private static final String VALID_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String VALID_JWKS_URI = "https://xyz.supabase.co/.well-known/jwks.json";

    // ========== Constructor Tests ==========

    @Test
    void GIVEN_validParameters_WHEN_constructorCalled_THEN_instanceCreated() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);

        // THEN
        assertThat(properties).isNotNull();
        assertThat(properties.url()).isEqualTo(VALID_URL);
        assertThat(properties.serviceKey()).isEqualTo(VALID_SERVICE_KEY);
        assertThat(properties.auth()).isEqualTo(auth);
        assertThat(properties.auth().jwkSetUri()).isEqualTo(VALID_JWKS_URI);
    }

    @Test
    void GIVEN_nullAuth_WHEN_constructorCalled_THEN_defaultAuthCreated() {
        // GIVEN / WHEN
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, null);

        // THEN
        assertThat(properties.auth()).isNotNull();
        assertThat(properties.auth().jwkSetUri()).isNull();
    }

    @Test
    void GIVEN_nullUrl_WHEN_constructorCalled_THEN_throwsNullPointerException() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThatThrownBy(() -> new SupabaseProperties(null, VALID_SERVICE_KEY, auth))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("url");
    }

    @Test
    void GIVEN_nullServiceKey_WHEN_constructorCalled_THEN_throwsNullPointerException() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThatThrownBy(() -> new SupabaseProperties(VALID_URL, null, auth))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("serviceKey");
    }

    // ========== Auth Record Tests ==========

    @Test
    void GIVEN_validJwksUri_WHEN_authConstructorCalled_THEN_authInstanceCreated() {
        // WHEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // THEN
        assertThat(auth).isNotNull();
        assertThat(auth.jwkSetUri()).isEqualTo(VALID_JWKS_URI);
    }

    @Test
    void GIVEN_nullJwksUri_WHEN_authConstructorCalled_THEN_authInstanceCreatedWithNull() {
        // WHEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(null);

        // THEN
        assertThat(auth).isNotNull();
        assertThat(auth.jwkSetUri()).isNull();
    }

    // ========== Equals and HashCode Tests ==========

    @Test
    void GIVEN_sameValues_WHEN_equalsChecked_THEN_objectsAreEqual() {
        // GIVEN
        SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties props1 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth1);
        SupabaseProperties props2 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth2);

        // WHEN / THEN
        assertThat(props1).isEqualTo(props2);
        assertThat(props1.hashCode()).isEqualTo(props2.hashCode());
    }

    @Test
    void GIVEN_differentUrl_WHEN_equalsChecked_THEN_objectsAreNotEqual() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties props1 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);
        SupabaseProperties props2 = new SupabaseProperties("https://different.supabase.co", VALID_SERVICE_KEY, auth);

        // WHEN / THEN
        assertThat(props1).isNotEqualTo(props2);
        assertThat(props1.hashCode()).isNotEqualTo(props2.hashCode());
    }

    @Test
    void GIVEN_differentServiceKey_WHEN_equalsChecked_THEN_objectsAreNotEqual() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties props1 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);
        SupabaseProperties props2 = new SupabaseProperties(VALID_URL, "differentKey", auth);

        // WHEN / THEN
        assertThat(props1).isNotEqualTo(props2);
        assertThat(props1.hashCode()).isNotEqualTo(props2.hashCode());
    }

    @Test
    void GIVEN_differentAuth_WHEN_equalsChecked_THEN_objectsAreNotEqual() {
        // GIVEN
        SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth("https://different.jwks.json");
        SupabaseProperties props1 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth1);
        SupabaseProperties props2 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth2);

        // WHEN / THEN
        assertThat(props1).isNotEqualTo(props2);
        assertThat(props1.hashCode()).isNotEqualTo(props2.hashCode());
    }

    @Test
    void GIVEN_sameAuthValues_WHEN_authEqualsChecked_THEN_authObjectsAreEqual() {
        // GIVEN
        SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThat(auth1).isEqualTo(auth2);
        assertThat(auth1.hashCode()).isEqualTo(auth2.hashCode());
    }

    @Test
    void GIVEN_differentAuthValues_WHEN_authEqualsChecked_THEN_authObjectsAreNotEqual() {
        // GIVEN
        SupabaseProperties.Auth auth1 = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties.Auth auth2 = new SupabaseProperties.Auth("https://different.jwks.json");

        // WHEN / THEN
        assertThat(auth1).isNotEqualTo(auth2);
        assertThat(auth1.hashCode()).isNotEqualTo(auth2.hashCode());
    }

    // ========== ToString Tests ==========

    @Test
    void GIVEN_validProperties_WHEN_toStringCalled_THEN_containsAllFields() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);

        // WHEN
        String result = properties.toString();

        // THEN
        assertThat(result)
                .contains("SupabaseProperties")
                .contains(VALID_URL)
                .contains(VALID_SERVICE_KEY)
                .contains("Auth");
    }

    @Test
    void GIVEN_validAuth_WHEN_authToStringCalled_THEN_containsJwkSetUri() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN
        String result = auth.toString();

        // THEN
        assertThat(result)
                .contains("Auth")
                .contains(VALID_JWKS_URI);
    }

    @Test
    void GIVEN_authWithNullJwksUri_WHEN_authToStringCalled_THEN_containsNull() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(null);

        // WHEN
        String result = auth.toString();

        // THEN
        assertThat(result)
                .contains("Auth")
                .contains("null");
    }

    // ========== Edge Cases ==========

    @Test
    void GIVEN_emptyStrings_WHEN_constructorCalled_THEN_instanceCreatedWithEmptyValues() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth("");

        // WHEN
        SupabaseProperties properties = new SupabaseProperties("", "", auth);

        // THEN
        assertThat(properties.url()).isEmpty();
        assertThat(properties.serviceKey()).isEmpty();
        assertThat(properties.auth().jwkSetUri()).isEmpty();
    }

    @Test
    void GIVEN_nullAuthInCompactConstructor_WHEN_constructorCalledTwice_THEN_bothGetDefaultAuth() {
        // GIVEN / WHEN
        SupabaseProperties props1 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, null);
        SupabaseProperties props2 = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, null);

        // THEN
        assertThat(props1.auth()).isNotNull();
        assertThat(props2.auth()).isNotNull();
        assertThat(props1.auth()).isEqualTo(props2.auth());
    }

    @Test
    void GIVEN_sameInstance_WHEN_equalsChecked_THEN_returnsTrue() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);

        // WHEN / THEN
        assertThat(properties).isEqualTo(properties);
    }

    @Test
    void GIVEN_nullComparison_WHEN_equalsChecked_THEN_returnsFalse() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);

        // WHEN / THEN
        assertThat(properties).isNotEqualTo(null);
    }

    @Test
    void GIVEN_differentClass_WHEN_equalsChecked_THEN_returnsFalse() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);
        SupabaseProperties properties = new SupabaseProperties(VALID_URL, VALID_SERVICE_KEY, auth);

        // WHEN / THEN
        assertThat(properties).isNotEqualTo("NotASupabaseProperties");
    }

    @Test
    void GIVEN_sameAuthInstance_WHEN_authEqualsChecked_THEN_returnsTrue() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThat(auth).isEqualTo(auth);
    }

    @Test
    void GIVEN_nullAuthComparison_WHEN_authEqualsChecked_THEN_returnsFalse() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThat(auth).isNotEqualTo(null);
    }

    @Test
    void GIVEN_differentAuthClass_WHEN_authEqualsChecked_THEN_returnsFalse() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(VALID_JWKS_URI);

        // WHEN / THEN
        assertThat(auth).isNotEqualTo("NotAnAuth");
    }
}
