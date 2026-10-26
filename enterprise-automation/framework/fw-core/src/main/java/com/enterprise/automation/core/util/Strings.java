package com.enterprise.automation.core.util;

import java.util.Objects;

/** Null- and blank-safe string helpers. */
public final class Strings {

  private Strings() {}

  /**
   * @param value candidate string, may be {@code null}
   * @return {@code true} if the value is {@code null} or contains only whitespace
   */
  public static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  /**
   * @param value candidate string, may be {@code null}
   * @return {@code true} if the value contains non-whitespace characters
   */
  public static boolean isNotBlank(String value) {
    return !isBlank(value);
  }

  /**
   * @param value value to check
   * @param name name used in the exception message
   * @return the value, unchanged
   * @throws IllegalArgumentException if the value is blank
   */
  public static String requireNonBlank(String value, String name) {
    if (isBlank(value)) {
      throw new IllegalArgumentException(
          Objects.requireNonNullElse(name, "value") + " must not be blank");
    }
    return value;
  }
}
