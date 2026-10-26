package com.enterprise.automation.steps.storefront;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.core.secrets.EnvSecretsProvider;
import com.enterprise.automation.core.security.SecretMasker;
import com.enterprise.automation.pages.saucedemo.InventoryPage;
import com.enterprise.automation.pages.saucedemo.LoginPage;
import io.qameta.allure.Step;

/**
 * Business flows for the storefront UI. Tests call these flows and never touch pages or WebDriver
 * directly. Passwords are read from the secrets provider and registered for masking.
 */
public final class StorefrontSteps {

  private final FrameworkConfig config;
  private final LoginPage loginPage;

  public StorefrontSteps() {
    this.config = FrameworkConfig.current();
    this.loginPage = new LoginPage();
  }

  /**
   * Logs in as the standard user from configuration.
   *
   * @return the inventory page reached after login
   */
  @Step("Log in as standard user")
  public InventoryPage loginAsStandardUser() {
    return login(config.require("ui.username"), password());
  }

  /**
   * Attempts a login as the locked-out user from configuration. The login is expected to fail.
   *
   * @return the login page, now showing an error
   */
  @Step("Attempt login as locked-out user")
  public LoginPage attemptLockedOutLogin() {
    loginPage.open(config.require("ui.base.url"));
    loginPage.login(config.require("ui.locked.username"), password());
    return loginPage;
  }

  private InventoryPage login(String username, String password) {
    loginPage.open(config.require("ui.base.url"));
    loginPage.login(username, password);
    return new InventoryPage();
  }

  private static String password() {
    String password = new EnvSecretsProvider().require("ui.password");
    SecretMasker.register(password);
    return password;
  }
}
