package com.enterprise.automation.core.config;

import com.enterprise.automation.core.util.Strings;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Typed, immutable view over the resolved framework configuration.
 *
 * <p>Obtain the process-wide instance with {@link #current()}. Tests may build an isolated instance
 * with {@link #of(Map)}. Values are validated on access so that misconfiguration fails fast with a
 * clear message.
 */
public final class FrameworkConfig {

  public static final String ENVIRONMENT = "env";
  public static final String TARGET = "target";
  public static final String BROWSER = "browser";
  public static final String HEADLESS = "headless";
  public static final String MOCK_PROFILE = "mock.profile";
  public static final String THREADS = "threads";
  public static final String GROUPS = "groups";

  private static final Set<String> TARGETS = Set.of("local", "docker-grid", "k8s-grid", "cloud");
  private static final Set<String> BROWSERS = Set.of("chrome", "firefox", "edge", "safari");
  private static final Set<String> MOCK_PROFILES = Set.of("real", "mock", "hybrid");

  private static volatile FrameworkConfig instance;

  private final Map<String, String> values;

  private FrameworkConfig(Map<String, String> values) {
    this.values = Map.copyOf(values);
  }

  /**
   * Creates a configuration from explicit values, bypassing the layered loader. Intended for tests.
   *
   * @param values configuration entries
   * @return a new immutable configuration
   */
  public static FrameworkConfig of(Map<String, String> values) {
    return new FrameworkConfig(values);
  }

  /**
   * Returns the process-wide configuration, loading it on first access.
   *
   * @return the cached configuration
   */
  public static FrameworkConfig current() {
    FrameworkConfig local = instance;
    if (local == null) {
      synchronized (FrameworkConfig.class) {
        local = instance;
        if (local == null) {
          local = load();
          instance = local;
        }
      }
    }
    return local;
  }

  /**
   * Clears the cached process-wide configuration so the next {@link #current()} call reloads it.
   */
  public static synchronized void reset() {
    instance = null;
  }

  private static FrameworkConfig load() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = FrameworkConfig.class.getClassLoader();
    }
    Map<String, String> system = new java.util.HashMap<>();
    System.getProperties().forEach((k, v) -> system.put(String.valueOf(k), String.valueOf(v)));
    return new FrameworkConfig(new ConfigLoader(classLoader).load(system, System.getenv()));
  }

  /**
   * @param key configuration key
   * @return the raw value if present
   */
  public Optional<String> find(String key) {
    return Optional.ofNullable(values.get(key));
  }

  /**
   * @param key configuration key
   * @return the raw value
   * @throws IllegalStateException if the key is absent or blank
   */
  public String require(String key) {
    String value = values.get(key);
    if (Strings.isBlank(value)) {
      throw new IllegalStateException("Missing configuration key: " + key);
    }
    return value.trim();
  }

  /**
   * @return the environment name, e.g. {@code qa}
   */
  public String environment() {
    return require(ENVIRONMENT);
  }

  /**
   * @return the execution target, one of {@code local, docker-grid, k8s-grid, cloud}
   * @throws IllegalArgumentException if the value is not supported
   */
  public String target() {
    return oneOf(TARGET, TARGETS);
  }

  /**
   * @return the browser, one of {@code chrome, firefox, edge, safari}
   * @throws IllegalArgumentException if the value is not supported
   */
  public String browser() {
    return oneOf(BROWSER, BROWSERS);
  }

  /**
   * @return {@code true} for headless execution
   */
  public boolean headless() {
    String value = require(HEADLESS).toLowerCase(java.util.Locale.ROOT);
    if (!value.equals("true") && !value.equals("false")) {
      throw new IllegalArgumentException("Invalid boolean for " + HEADLESS + ": " + value);
    }
    return Boolean.parseBoolean(value);
  }

  /**
   * @return the downstream mock profile, one of {@code real, mock, hybrid} (lower-case)
   * @throws IllegalArgumentException if the value is not supported
   */
  public String mockProfile() {
    return oneOf(MOCK_PROFILE, MOCK_PROFILES);
  }

  /**
   * @return the number of parallel threads, always at least 1
   */
  public int threads() {
    String raw = require(THREADS);
    int parsed;
    try {
      parsed = Integer.parseInt(raw);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid integer for " + THREADS + ": " + raw, e);
    }
    if (parsed < 1) {
      throw new IllegalArgumentException(THREADS + " must be >= 1 but was " + parsed);
    }
    return parsed;
  }

  /**
   * @return the TestNG group expression, if one is configured
   */
  public Optional<String> groups() {
    return find(GROUPS).filter(Strings::isNotBlank).map(String::trim);
  }

  private String oneOf(String key, Set<String> allowed) {
    String value = require(key).toLowerCase(java.util.Locale.ROOT);
    if (!allowed.contains(value)) {
      throw new IllegalArgumentException(
          "Unsupported " + key + " '" + value + "'. Allowed: " + allowed);
    }
    return value;
  }
}
