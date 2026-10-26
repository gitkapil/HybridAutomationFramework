package com.enterprise.automation.pages.saucedemo;

import com.enterprise.automation.ui.page.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Page object for the saucedemo.com login page. Exposes actions and state; assertions live in
 * tests.
 */
public final class LoginPage extends BasePage {

  private static final By USERNAME = By.id("user-name");
  private static final By PASSWORD = By.id("password");
  private static final By LOGIN_BUTTON = By.id("login-button");
  private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

  /**
   * Opens the login page at the given base URL.
   *
   * @param baseUrl application base URL
   * @return this page
   */
  @Step("Open login page")
  public LoginPage open(String baseUrl) {
    actions.open(baseUrl);
    return this;
  }

  /**
   * @param username account name
   * @param password account password
   */
  @Step("Submit credentials for '{username}'")
  public void login(String username, String password) {
    actions.type(USERNAME, username);
    actions.type(PASSWORD, password);
    actions.click(LOGIN_BUTTON);
  }

  /**
   * @return the visible error banner text, or an empty string if no error is shown
   */
  public String errorMessage() {
    return actions.isDisplayed(ERROR_MESSAGE) ? actions.text(ERROR_MESSAGE) : "";
  }
}
