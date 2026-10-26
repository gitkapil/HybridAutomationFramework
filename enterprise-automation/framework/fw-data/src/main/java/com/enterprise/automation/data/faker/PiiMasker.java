package com.enterprise.automation.data.faker;

/**
 * Masks personal data before it reaches logs or reports. Keeps just enough shape to debug a
 * failure.
 */
public final class PiiMasker {

  private static final String MASK = "***";

  private PiiMasker() {}

  /**
   * Masks the local part of an email address, keeping the first character and the domain. Values
   * without an {@code @} are fully masked.
   *
   * @param email raw email address, may be {@code null}
   * @return masked address, for example {@code a***@example.com}; {@code null} for {@code null}
   *     input
   */
  public static String maskEmail(String email) {
    if (email == null) {
      return null;
    }
    int at = email.indexOf('@');
    if (at <= 0) {
      return MASK;
    }
    return email.charAt(0) + MASK + email.substring(at);
  }
}
