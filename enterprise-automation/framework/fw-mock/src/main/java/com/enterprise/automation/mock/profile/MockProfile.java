package com.enterprise.automation.mock.profile;

import java.util.Locale;

/**
 * How a downstream service is exercised for a run.
 *
 * <ul>
 *   <li>{@link #REAL} calls the live service.
 *   <li>{@link #MOCK} calls a WireMock stand-in.
 *   <li>{@link #HYBRID} mocks only the services listed in {@code mock.hybrid.services}.
 * </ul>
 */
public enum MockProfile {
  REAL,
  MOCK,
  HYBRID;

  /**
   * @param raw configuration value, case-insensitive
   * @return the matching profile
   * @throws IllegalArgumentException for unknown values
   */
  public static MockProfile parse(String raw) {
    try {
      return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(
          "Unknown mock profile '" + raw + "' (expected real, mock or hybrid)", e);
    }
  }
}
