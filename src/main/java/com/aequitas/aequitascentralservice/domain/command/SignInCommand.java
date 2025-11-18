package com.aequitas.aequitascentralservice.domain.command;

import lombok.Builder;

/**
 * Command capturing user supplied credentials for Supabase password authentication.
 *
 * @param email login email address.
 * @param password plaintext password to validate.
 */
@Builder
public record SignInCommand(String email, String password) {}
