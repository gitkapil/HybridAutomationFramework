package com.enterprise.automation.data.faker;

import com.github.javafaker.Faker;
import java.util.Locale;
import java.util.UUID;

/**
 * Thread-safe factory for realistic, unique test values. Each thread gets its own {@link Faker}
 * because the library is not guaranteed to be thread-safe.
 */
public final class FakeData {

  private static final ThreadLocal<Faker> FAKER =
      ThreadLocal.withInitial(() -> new Faker(Locale.ENGLISH));

  private FakeData() {}

  /**
   * @return a random first name
   */
  public static String firstName() {
    return FAKER.get().name().firstName();
  }

  /**
   * @return a random last name
   */
  public static String lastName() {
    return FAKER.get().name().lastName();
  }

  /**
   * @return a random email address on the reserved {@code example.com} domain
   */
  public static String email() {
    return uniqueSuffix() + "@example.com";
  }

  /**
   * @param prefix leading text, for example a test or entity name
   * @return the prefix followed by an 8-character random suffix that is unique for practical
   *     purposes
   */
  public static String uniqueName(String prefix) {
    return prefix + uniqueSuffix();
  }

  /**
   * @return an 8-character random hexadecimal string
   */
  public static String uniqueSuffix() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}
