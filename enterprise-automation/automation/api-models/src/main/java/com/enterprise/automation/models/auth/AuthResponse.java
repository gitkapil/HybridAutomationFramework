package com.enterprise.automation.models.auth;

/**
 * Response of {@code POST /auth}.
 *
 * @param token session token
 */
public record AuthResponse(String token) {}
