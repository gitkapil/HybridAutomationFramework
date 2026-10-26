package com.enterprise.automation.mock.profile;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves the effective {@link MockProfile} for a named downstream service.
 *
 * <p>Precedence: {@code mock.<service>.profile} if set, otherwise {@code mock.profile}. Under
 * HYBRID, a service is mocked only when it is listed in {@code mock.hybrid.services}
 * (comma-separated); other services stay REAL.
 */
public final class MockProfiles {

  private MockProfiles() {}

  /**
   * @param service logical service name, for example {@code booking}
   * @param config configuration to read from
   * @return the effective profile for {@code service}
   */
  public static MockProfile resolve(String service, FrameworkConfig config) {
    String key = service.toLowerCase(Locale.ROOT);
    MockProfile global = MockProfile.parse(config.find("mock.profile").orElse("real"));
    MockProfile effective =
        config.find("mock." + key + ".profile").map(MockProfile::parse).orElse(global);
    if (effective == MockProfile.HYBRID) {
      return hybridServices(config).contains(key) ? MockProfile.MOCK : MockProfile.REAL;
    }
    return effective;
  }

  /**
   * @param service logical service name
   * @return {@code true} if the service should be served by WireMock for the current configuration
   */
  public static boolean isMocked(String service) {
    return resolve(service, FrameworkConfig.current()) == MockProfile.MOCK;
  }

  private static Set<String> hybridServices(FrameworkConfig config) {
    return Arrays.stream(config.find("mock.hybrid.services").orElse("").split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }
}
