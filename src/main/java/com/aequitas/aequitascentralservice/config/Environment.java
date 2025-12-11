package com.aequitas.aequitascentralservice.config;

import jakarta.annotation.Nonnull;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
public enum Environment {
    DEV("dev"),
    BETA("beta"),
    GAMMA("gamma"),
    PROD("prod");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public static Environment fromString(String value) {
        for (Environment environment : Environment.values()) {
            if (environment.getValue().equalsIgnoreCase(value)) {
                return environment;
            }
        }
        throw new IllegalArgumentException("No environment with value: " + value);
    }

    public static Set<Environment> profiles(Environment... environments) {
        return Set.of(environments);
    }

    public static boolean isDevEnvironment(@Nonnull Environment environment) {
        return profiles(DEV, BETA).contains(environment);
    }

    public static boolean isDevEnvironment(@Nonnull String environment) {
        return isDevEnvironment(fromString(environment));
    }
}
