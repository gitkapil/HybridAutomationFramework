package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/**
 * Starts browsers on the local machine. Driver binaries are resolved automatically by Selenium
 * Manager.
 */
public final class LocalDriverProvider implements DriverProvider {

  @Override
  public WebDriver create(FrameworkConfig config) {
    String browser = config.browser();
    MutableCapabilities options = BrowserOptionsFactory.create(browser, config.headless());
    return switch (browser) {
      case "chrome" -> new ChromeDriver((ChromeOptions) options);
      case "firefox" -> new FirefoxDriver((FirefoxOptions) options);
      case "edge" -> new EdgeDriver((EdgeOptions) options);
      case "safari" -> new SafariDriver((SafariOptions) options);
      default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
    };
  }
}
