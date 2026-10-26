package com.enterprise.automation.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.testng.annotations.Test;

/**
 * Verifies the precedence chain: defaults &lt; env file &lt; system properties &lt; FW_ environment
 * variables.
 */
@Test(groups = "unit")
public class ConfigLoaderTest {

  private final ConfigLoader loader = new ConfigLoader(getClass().getClassLoader());

  @Test(
      description =
          "Defaults apply when no overrides are present, and env=dev loads dev.properties")
  public void defaultsApplyWhenNoOverrides() {
    Map<String, String> config = loader.load(Map.of(), Map.of());

    assertThat(config)
        .containsEntry("env", "dev")
        .containsEntry("target", "local")
        .containsEntry("mock.profile", "real")
        .containsEntry("base.url", "https://dev.example.test");
  }

  @Test(description = "Environment file overrides defaults")
  public void envFileOverridesDefaults() {
    Map<String, String> config = loader.load(Map.of("env", "qa"), Map.of());

    assertThat(config)
        .containsEntry("base.url", "https://qa.example.test")
        .containsEntry("browser", "firefox");
  }

  @Test(description = "System property overrides env file")
  public void systemPropertyOverridesEnvFile() {
    Map<String, String> config = loader.load(Map.of("env", "qa", "browser", "edge"), Map.of());

    assertThat(config).containsEntry("browser", "edge");
  }

  @Test(description = "FW_ environment variable overrides system property")
  public void environmentVariableOverridesSystemProperty() {
    Map<String, String> config =
        loader.load(Map.of("env", "qa", "browser", "edge"), Map.of("FW_BROWSER", "safari"));

    assertThat(config).containsEntry("browser", "safari");
  }

  @Test(description = "FW_ environment variables map underscores to dots and are lower-cased")
  public void environmentVariableNameMapping() {
    Map<String, String> config = loader.load(Map.of(), Map.of("FW_MOCK_PROFILE", "mock"));

    assertThat(config).containsEntry("mock.profile", "mock");
  }

  @Test(description = "Environment variables without the FW_ prefix are ignored")
  public void unprefixedEnvironmentVariablesIgnored() {
    Map<String, String> config = loader.load(Map.of(), Map.of("BROWSER", "safari"));

    assertThat(config).containsEntry("browser", "chrome");
  }

  @Test(description = "Missing environment file fails fast")
  public void missingEnvironmentFileFails() {
    assertThatThrownBy(() -> loader.load(Map.of("env", "uat"), Map.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("config/uat.properties");
  }

  @Test(description = "Path-traversal style environment names are rejected")
  public void invalidEnvironmentNameRejected() {
    assertThatThrownBy(() -> loader.load(Map.of("env", "../secrets"), Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid environment name");
  }

  @Test(description = "FrameworkConfig rejects unsupported targets")
  public void typedAccessorValidatesTarget() {
    FrameworkConfig config = FrameworkConfig.of(Map.of("target", "moon"));

    assertThatThrownBy(config::target)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported target");
  }

  @Test(description = "FrameworkConfig rejects non-positive thread counts")
  public void typedAccessorValidatesThreads() {
    FrameworkConfig config = FrameworkConfig.of(Map.of("threads", "0"));

    assertThatThrownBy(config::threads)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be >= 1");
  }

  @Test(description = "FrameworkConfig returns typed values and normalises case")
  public void typedAccessorsReturnNormalisedValues() {
    FrameworkConfig config =
        FrameworkConfig.of(
            Map.of(
                "target", "Docker-Grid",
                "browser", "CHROME",
                "headless", "TRUE",
                "mock.profile", "Hybrid",
                "threads", "4",
                "groups", "  "));

    assertThat(config.target()).isEqualTo("docker-grid");
    assertThat(config.browser()).isEqualTo("chrome");
    assertThat(config.headless()).isTrue();
    assertThat(config.mockProfile()).isEqualTo("hybrid");
    assertThat(config.threads()).isEqualTo(4);
    assertThat(config.groups()).isEmpty();
  }
}
