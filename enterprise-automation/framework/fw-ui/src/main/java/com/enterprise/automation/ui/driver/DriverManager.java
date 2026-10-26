package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.time.Duration;
import org.openqa.selenium.WebDriver;

/**
 * Holds one {@link WebDriver} per thread so parallel tests never share a browser. Implicit waits
 * are disabled; all waiting goes through explicit waits in {@code Waits}.
 */
public final class DriverManager {

  private static final DriverFactory FACTORY = new DriverFactory();
  private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

  private DriverManager() {}

  /**
   * Creates and binds a driver to the current thread. Any previously bound driver is quit first.
   *
   * @return the new driver
   */
  public static WebDriver start() {
    quit();
    FrameworkConfig config = FrameworkConfig.current();
    WebDriver driver = FACTORY.create(config);
    driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    driver.manage().window().maximize();
    DRIVER.set(driver);
    return driver;
  }

  /**
   * @return the driver bound to the current thread
   * @throws IllegalStateException if {@link #start()} has not been called on this thread
   */
  public static WebDriver get() {
    WebDriver driver = DRIVER.get();
    if (driver == null) {
      throw new IllegalStateException(
          "No WebDriver bound to thread " + Thread.currentThread().getName());
    }
    return driver;
  }

  /** Quits and unbinds the current thread's driver. Safe to call when none is bound. */
  public static void quit() {
    WebDriver driver = DRIVER.get();
    if (driver != null) {
      try {
        driver.quit();
      } finally {
        DRIVER.remove();
      }
    }
  }
}
