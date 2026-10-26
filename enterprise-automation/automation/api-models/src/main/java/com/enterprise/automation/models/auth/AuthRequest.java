package com.enterprise.automation.models.auth;

/**
 * Credentials posted to {@code /auth} to obtain a session token.
 *
 * @param username account name
 * @param password account password
 */
public record AuthRequest(String username, String password) {}
