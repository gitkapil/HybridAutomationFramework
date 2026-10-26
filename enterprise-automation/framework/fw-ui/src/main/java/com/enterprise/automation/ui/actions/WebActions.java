package com.enterprise.automation.ui.actions;

import com.enterprise.automation.ui.wait.Waits;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Wait-aware wrapper around common browser interactions. Pages use this instead of raw WebDriver
 * calls. Methods perform no assertions; they only act and report state.
 */
public final class WebActions {

  private final WebDriver driver;
  private final Waits waits;

  /**
   * @param driver driver to act on
   * @param timeout explicit-wait timeout
   */
  public WebActions(WebDriver driver, Duration timeout) {
    this.driver = Objects.requireNonNull(driver, "driver");
    this.waits = new Waits(driver, timeout);
  }

  /**
   * @param url absolute URL to navigate to
   */
  public void open(String url) {
    driver.get(url);
  }

  /**
   * @param by locator of the element to click, waiting until it is clickable
   */
  public void click(By by) {
    waits.clickable(by).click();
  }

  /**
   * Clears the field and types the text.
   *
   * @param by locator of the input
   * @param text text to type
   */
  public void type(By by, String text) {
    WebElement element = waits.visible(by);
    element.clear();
    element.sendKeys(text);
  }

  /**
   * @param by locator
   * @return the visible text of the element
   */
  public String text(By by) {
    return waits.visible(by).getText();
  }

  /**
   * @param by locator
   * @return {@code true} if the element is visible within the timeout
   */
  public boolean isDisplayed(By by) {
    return waits.isVisible(by);
  }

  /**
   * @param by locator
   * @param text expected text, waited for until it matches
   */
  public void waitForText(By by, String text) {
    waits.textEquals(by, text);
  }

  /**
   * @param fragment expected substring of the current URL
   */
  public void waitForUrlContains(String fragment) {
    waits.urlContains(fragment);
  }

  /**
   * @param by locator
   * @return all currently present matching elements, without waiting
   */
  public List<WebElement> findAll(By by) {
    return driver.findElements(by);
  }
}
