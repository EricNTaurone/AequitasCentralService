package com.aequitas.aequitascentralservice.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class JacksonConfigTest {

    private static final String TEST_INSTANT_STRING = "2023-11-17T10:15:30.123Z";
    private static final String TEST_LOCAL_DATE_STRING = "2023-11-17";
    private static final String TEST_LOCAL_DATETIME_STRING = "2023-11-17T10:15:30.123";

    private JacksonConfig jacksonConfig;

    @BeforeEach
    void setUp() {
        jacksonConfig = new JacksonConfig();
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_ReturnNonNullObjectMapper() {
        // GIVEN
        // jacksonConfig is initialized in setUp

        // WHEN
        final ObjectMapper result = jacksonConfig.objectMapper();

        // THEN
        assertNotNull(result, "ObjectMapper should not be null");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_WriteDatesAsTimestampsIsDisabled() {
        // GIVEN
        // jacksonConfig is initialized in setUp

        // WHEN
        final ObjectMapper result = jacksonConfig.objectMapper();

        // THEN
        assertFalse(result.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
                "WRITE_DATES_AS_TIMESTAMPS should be disabled to serialize dates as ISO-8601 strings");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_JavaTimeModuleIsRegistered() throws JsonProcessingException {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final Instant testInstant = Instant.parse(TEST_INSTANT_STRING);

        // WHEN
        final String serialized = mapper.writeValueAsString(testInstant);

        // THEN
        assertNotNull(serialized, "Serialized instant should not be null");
        assertFalse(serialized.isEmpty(), "Serialized instant should not be empty");
        assertTrue(serialized.contains(TEST_INSTANT_STRING.replace("Z", "")), 
                "Serialized instant should contain the expected timestamp");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanSerializeLocalDate() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final LocalDate testDate = LocalDate.parse(TEST_LOCAL_DATE_STRING);

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final String serialized = mapper.writeValueAsString(testDate);
            assertNotNull(serialized, "Serialized LocalDate should not be null");
            assertFalse(serialized.isEmpty(), "Serialized LocalDate should not be empty");
        }, "Should be able to serialize LocalDate without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanSerializeLocalDateTime() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final LocalDateTime testDateTime = LocalDateTime.parse(TEST_LOCAL_DATETIME_STRING);

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final String serialized = mapper.writeValueAsString(testDateTime);
            assertNotNull(serialized, "Serialized LocalDateTime should not be null");
            assertFalse(serialized.isEmpty(), "Serialized LocalDateTime should not be empty");
        }, "Should be able to serialize LocalDateTime without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanSerializeZonedDateTime() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final ZonedDateTime testZonedDateTime = ZonedDateTime.parse("2023-11-17T10:15:30.123+01:00[Europe/Paris]");

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final String serialized = mapper.writeValueAsString(testZonedDateTime);
            assertNotNull(serialized, "Serialized ZonedDateTime should not be null");
            assertFalse(serialized.isEmpty(), "Serialized ZonedDateTime should not be empty");
        }, "Should be able to serialize ZonedDateTime without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanDeserializeInstant() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final String instantJson = "\"" + TEST_INSTANT_STRING + "\"";

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final Instant deserialized = mapper.readValue(instantJson, Instant.class);
            assertNotNull(deserialized, "Deserialized Instant should not be null");
        }, "Should be able to deserialize Instant without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanDeserializeLocalDate() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final String localDateJson = "\"" + TEST_LOCAL_DATE_STRING + "\"";

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final LocalDate deserialized = mapper.readValue(localDateJson, LocalDate.class);
            assertNotNull(deserialized, "Deserialized LocalDate should not be null");
        }, "Should be able to deserialize LocalDate without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_ObjectMapperCalled_THEN_CanDeserializeLocalDateTime() {
        // GIVEN
        // jacksonConfig is initialized in setUp
        final ObjectMapper mapper = jacksonConfig.objectMapper();
        final String localDateTimeJson = "\"" + TEST_LOCAL_DATETIME_STRING + "\"";

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            final LocalDateTime deserialized = mapper.readValue(localDateTimeJson, LocalDateTime.class);
            assertNotNull(deserialized, "Deserialized LocalDateTime should not be null");
        }, "Should be able to deserialize LocalDateTime without exception");
    }

    @Test
    void GIVEN_JacksonConfig_WHEN_MultipleCallsToObjectMapper_THEN_EachReturnsNewInstance() {
        // GIVEN
        // jacksonConfig is initialized in setUp

        // WHEN
        final ObjectMapper mapper1 = jacksonConfig.objectMapper();
        final ObjectMapper mapper2 = jacksonConfig.objectMapper();

        // THEN
        assertNotNull(mapper1, "First ObjectMapper should not be null");
        assertNotNull(mapper2, "Second ObjectMapper should not be null");
        assertFalse(mapper1 == mapper2, "Each call should return a new instance (not Spring-managed singleton in this test)");
    }
}
