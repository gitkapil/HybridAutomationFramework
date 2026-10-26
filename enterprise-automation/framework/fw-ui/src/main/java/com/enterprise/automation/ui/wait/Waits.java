package com.enterprise.automation.ui.wait;

import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Explicit-wait library. Every wait is bounded by the configured timeout; there is no {@code
 * Thread.sleep}.
 */
public final class Waits {

  private final WebDriver driver;
  private final Duration timeout;

  /**
   * @param driver driver to wait on
   * @param timeout maximum time to wait for any condition
   */
  public Waits(WebDriver driver, Duration timeout) {
    this.driver = Objects.requireNonNull(driver, "driver");
    this.timeout = Objects.requireNonNull(timeout, "timeout");
  }

  /**
   * @param by locator
   * @return the element once it is visible
   */
  public WebElement visible(By by) {
    return explicit().until(ExpectedConditions.visibilityOfElementLocated(by));
  }

  /**
   * @param by locator
   * @return the element once it is visible and enabled for clicking
   */
  public WebElement clickable(By by) {
    return explicit().until(ExpectedConditions.elementToBeClickable(by));
  }

  /**
   * @param by locator
   * @param text expected text (exact match after trimming)
   */
  public void textEquals(By by, String text) {
    explicit().until(ExpectedConditions.textToBe(by, text));
  }

  /**
   * @param by locator
   * @return {@code true} if the element becomes visible within the timeout, {@code false} otherwise
   */
  public boolean isVisible(By by) {
    try {
      visible(by);
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }

  /**
   * @param fragment expected substring of the current URL
   */
  public void urlContains(String fragment) {
    explicit().until(ExpectedConditions.urlContains(fragment));
  }

  private WebDriverWait explicit() {
    return new WebDriverWait(driver, timeout);
  }
}
