package com.enterprise.automation.tests.ui.a11y;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.pages.saucedemo.LoginPage;
import com.enterprise.automation.tests.ui.BaseUiTest;
import com.enterprise.automation.ui.a11y.AccessibilityChecker;
import com.enterprise.automation.ui.a11y.AccessibilityReport;
import com.enterprise.automation.ui.driver.DriverManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Storefront UI")
@Feature("Accessibility")
@Test(groups = "a11y")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class LoginAccessibilityTest extends BaseUiTest {

  @Test(groups = {"a11y", "regression"})
  @Story("WCAG 2.0 A/AA scan of the login page")
  @Description("The login page has no critical WCAG A or AA violations according to axe-core.")
  public void loginPageHasNoCriticalViolations() {
    LoginPage loginPage = new LoginPage().open(FrameworkConfig.current().require("ui.base.url"));

    AccessibilityReport report = new AccessibilityChecker().analyze(DriverManager.get());

    assertThat(report.withImpact("critical"))
        .as("critical accessibility violations on the login page")
        .isEmpty();
  }
}
