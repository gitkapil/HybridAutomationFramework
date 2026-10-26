package com.enterprise.automation.core.config;

import com.enterprise.automation.core.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Builds the effective configuration map using a fixed precedence (lowest to highest):
 *
 * <ol>
 *   <li>{@code framework-defaults.properties} from the classpath
 *   <li>{@code config/<env>.properties} from the classpath, where {@code <env>} is resolved from
 *       the layers below
 *   <li>JVM system properties, e.g. {@code -Dbrowser=firefox}
 *   <li>Environment variables prefixed with {@code FW_}, e.g. {@code FW_BROWSER=firefox}.
 *       Underscores become dots and the name is lower-cased, so {@code FW_MOCK_PROFILE} maps to
 *       {@code mock.profile}.
 * </ol>
 *
 * The loader is stateless apart from its class loader and is therefore thread-safe.
 */
public final class ConfigLoader {

  static final String DEFAULTS_RESOURCE = "framework-defaults.properties";
  static final String ENV_KEY = "env";
  static final String ENV_VAR_PREFIX = "FW_";
  private static final String ENV_FILE_PATTERN = "config/%s.properties";
  private static final Pattern VALID_ENV_NAME = Pattern.compile("[a-z0-9][a-z0-9-]*");

  private final ClassLoader classLoader;

  /**
   * @param classLoader class loader used to locate classpath resources
   */
  public ConfigLoader(ClassLoader classLoader) {
    this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
  }

  /**
   * Resolves the effective configuration.
   *
   * @param systemProperties JVM system properties (highest precedence except environment variables)
   * @param environmentVariables process environment variables; only {@code FW_}-prefixed entries
   *     are used
   * @return immutable merged configuration
   * @throws IllegalArgumentException if the resolved environment name is malformed
   * @throws IllegalStateException if the defaults or the environment file cannot be found
   */
  public Map<String, String> load(
      Map<String, String> systemProperties, Map<String, String> environmentVariables) {
    Map<String, String> defaults = readRequired(DEFAULTS_RESOURCE);
    Map<String, String> system = new LinkedHashMap<>(Objects.requireNonNull(systemProperties));
    Map<String, String> envVars =
        mapEnvironmentVariables(Objects.requireNonNull(environmentVariables));

    String envName = resolveEnvironmentName(defaults, system, envVars);
    Map<String, String> envFile =
        readRequired(String.format(Locale.ROOT, ENV_FILE_PATTERN, envName));

    Map<String, String> merged = new LinkedHashMap<>();
    merged.putAll(defaults);
    merged.putAll(envFile);
    merged.putAll(system);
    merged.putAll(envVars);
    return Map.copyOf(merged);
  }

  private static String resolveEnvironmentName(
      Map<String, String> defaults, Map<String, String> system, Map<String, String> envVars) {
    String name =
        envVars.getOrDefault(ENV_KEY, system.getOrDefault(ENV_KEY, defaults.get(ENV_KEY)));
    Strings.requireNonBlank(name, ENV_KEY);
    if (!VALID_ENV_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException(
          "Invalid environment name '"
              + name
              + "': only lower-case letters, digits and '-' are allowed");
    }
    return name;
  }

  static Map<String, String> mapEnvironmentVariables(Map<String, String> environmentVariables) {
    Map<String, String> mapped = new HashMap<>();
    for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
      String name = entry.getKey();
      if (name == null || !name.startsWith(ENV_VAR_PREFIX) || entry.getValue() == null) {
        continue;
      }
      String key =
          name.substring(ENV_VAR_PREFIX.length()).toLowerCase(Locale.ROOT).replace('_', '.');
      if (!key.isEmpty()) {
        mapped.put(key, entry.getValue());
      }
    }
    return mapped;
  }

  private Map<String, String> readRequired(String resource) {
    try (InputStream in = classLoader.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Missing configuration resource on classpath: " + resource);
      }
      Properties properties = new Properties();
      properties.load(in);
      Map<String, String> values = new HashMap<>();
      for (String key : properties.stringPropertyNames()) {
        values.put(key, properties.getProperty(key));
      }
      return values;
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read configuration resource: " + resource, e);
    }
  }
}
