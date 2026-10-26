package com.enterprise.automation.core.security;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide registry of secret values that must never appear in logs or reports.
 *
 * <p>Registration and masking are thread-safe. Longer secrets are replaced first so that
 * overlapping values are fully redacted.
 */
public final class SecretMasker {

  /** Replacement text written in place of a secret. */
  public static final String MASK = "****";

  private static final Set<String> SECRETS = ConcurrentHashMap.newKeySet();

  private SecretMasker() {}

  /**
   * Registers a value for masking. Null and blank values are ignored.
   *
   * @param secret the secret value
   */
  public static void register(String secret) {
    if (secret != null && !secret.isBlank()) {
      SECRETS.add(secret);
    }
  }

  /**
   * Replaces every registered secret occurrence in the text.
   *
   * @param text text that may contain secrets; may be {@code null}
   * @return masked text, or {@code null} if the input was {@code null}
   */
  public static String mask(String text) {
    if (text == null || SECRETS.isEmpty()) {
      return text;
    }
    String masked = text;
    List<String> ordered =
        SECRETS.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
    for (String secret : ordered) {
      masked = masked.replace(secret, MASK);
    }
    return masked;
  }

  /** Removes all registered secrets. Intended for tests only. */
  public static void clear() {
    SECRETS.clear();
  }
}
