package com.enterprise.automation.core.secrets;

import com.enterprise.automation.core.util.Strings;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves secrets from environment variables. The logical name {@code api.token} maps to the
 * variable {@code SECRET_API_TOKEN}. Dots and dashes become underscores and the name is
 * upper-cased.
 */
public final class EnvSecretsProvider implements SecretsProvider {

  static final String PREFIX = "SECRET_";

  private final Map<String, String> environment;

  /** Uses the process environment. */
  public EnvSecretsProvider() {
    this(System.getenv());
  }

  /**
   * @param environment variable source, injectable for tests
   */
  public EnvSecretsProvider(Map<String, String> environment) {
    this.environment = Map.copyOf(environment);
  }

  @Override
  public Optional<String> find(String name) {
    Strings.requireNonBlank(name, "secret name");
    String variable = PREFIX + name.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    return Optional.ofNullable(environment.get(variable)).filter(Strings::isNotBlank);
  }
}
