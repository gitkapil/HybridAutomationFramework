package com.enterprise.automation.ui.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariOptions;

/**
 * Builds browser-specific options. Headless mode is applied per browser where the browser supports
 * it.
 */
public final class BrowserOptionsFactory {

  private static final String WINDOW_SIZE = "--window-size=1920,1080";

  private BrowserOptionsFactory() {}

  /**
   * @param browser one of {@code chrome, firefox, edge, safari}
   * @param headless whether to run without a visible window
   * @return options for the given browser
   * @throws IllegalArgumentException if the browser is not supported
   */
  public static MutableCapabilities create(String browser, boolean headless) {
    return switch (browser) {
      case "chrome" -> {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
          options.addArguments("--headless=new");
        }
        options.addArguments(WINDOW_SIZE);
        yield options;
      }
      case "firefox" -> {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
          options.addArguments("-headless");
        }
        yield options;
      }
      case "edge" -> {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
          options.addArguments("--headless=new");
        }
        options.addArguments(WINDOW_SIZE);
        yield options;
      }
      case "safari" -> new SafariOptions();
      default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
    };
  }
}
