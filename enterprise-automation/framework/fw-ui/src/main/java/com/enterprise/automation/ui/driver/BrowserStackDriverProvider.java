package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.core.secrets.EnvSecretsProvider;
import com.enterprise.automation.core.security.SecretMasker;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Starts sessions on BrowserStack Automate. Selected with {@code -Dtarget=cloud}. Credentials come
 * from the {@code SECRET_BROWSERSTACK_USER} and {@code SECRET_BROWSERSTACK_KEY} environment
 * variables and are masked in logs. Hub, project, build and browser version are configuration keys
 * ({@code cloud.*}).
 */
public final class BrowserStackDriverProvider implements DriverProvider {

  static final String DEFAULT_HUB = "https://hub-cloud.browserstack.com/wd/hub";

  private final EnvSecretsProvider secrets;

  /** Uses the process environment for secrets. */
  public BrowserStackDriverProvider() {
    this(new EnvSecretsProvider());
  }

  /**
   * @param secrets secrets source, injectable for tests
   */
  public BrowserStackDriverProvider(EnvSecretsProvider secrets) {
    this.secrets = secrets;
  }

  @Override
  public WebDriver create(FrameworkConfig config) {
    String user = requireSecret("browserstack.user");
    String key = requireSecret("browserstack.key");
    SecretMasker.register(key);

    MutableCapabilities caps = BrowserOptionsFactory.create(config.browser(), false);
    config.find("cloud.browser.version").ifPresent(v -> caps.setCapability("browserVersion", v));

    Map<String, Object> bstack = new HashMap<>();
    bstack.put("userName", user);
    bstack.put("accessKey", key);
    bstack.put("projectName", config.find("cloud.project").orElse("enterprise-automation"));
    bstack.put("buildName", config.find("cloud.build").orElse("local-build"));
    caps.setCapability("bstack:options", bstack);

    return new RemoteWebDriver(hubUrl(config.find("cloud.url").orElse(DEFAULT_HUB)), caps);
  }

  private String requireSecret(String name) {
    return secrets
        .find(name)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Missing secret "
                        + "SECRET_"
                        + name.toUpperCase(java.util.Locale.ROOT).replace('.', '_')
                        + " required for -Dtarget=cloud"));
  }

  private static URL hubUrl(String value) {
    try {
      return URI.create(value).toURL();
    } catch (MalformedURLException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cloud.url: " + value, e);
    }
  }
}
