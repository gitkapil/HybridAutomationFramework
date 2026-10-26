package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.util.Map;
import org.openqa.selenium.WebDriver;

/**
 * Factory that resolves the {@link DriverProvider} for the configured {@code target} and creates a
 * driver. Cloud providers (BrowserStack) are registered as target {@code cloud}.
 */
public final class DriverFactory {

  private final Map<String, DriverProvider> providers =
      Map.of(
          "local", new LocalDriverProvider(),
          "docker-grid", new DockerGridDriverProvider(),
          "k8s-grid", new DockerGridDriverProvider(),
          "cloud", new BrowserStackDriverProvider());

  /**
   * @param config resolved configuration
   * @return a new driver for the configured target
   * @throws IllegalStateException if no provider is registered for the target
   */
  public WebDriver create(FrameworkConfig config) {
    DriverProvider provider = providers.get(config.target());
    if (provider == null) {
      throw new IllegalStateException(
          "No driver provider registered for target: " + config.target());
    }
    return provider.create(config);
  }
}
