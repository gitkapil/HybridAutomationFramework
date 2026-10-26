package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Starts sessions on a Selenium Grid 4 hub (Docker Compose or Kubernetes). The hub address comes
 * from the {@code grid.url} configuration key, so switching grids requires no code change.
 */
public final class DockerGridDriverProvider implements DriverProvider {

  @Override
  public WebDriver create(FrameworkConfig config) {
    URL hub = hubUrl(config.require("grid.url"));
    MutableCapabilities options = BrowserOptionsFactory.create(config.browser(), config.headless());
    return new RemoteWebDriver(hub, options);
  }

  private static URL hubUrl(String value) {
    try {
      return URI.create(value).toURL();
    } catch (MalformedURLException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid grid.url: " + value, e);
    }
  }
}
