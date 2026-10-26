package com.enterprise.automation.tests.ui.visual;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.pages.saucedemo.LoginPage;
import com.enterprise.automation.tests.ui.BaseUiTest;
import com.enterprise.automation.ui.driver.DriverManager;
import com.enterprise.automation.ui.visual.VisualComparator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Storefront UI")
@Feature("Visual regression")
@Test(groups = "visual")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class LoginVisualRegressionTest extends BaseUiTest {

  @Test(groups = {"visual", "regression"})
  @Story("Login page pixel comparison")
  @Description(
      "The login page matches its committed baseline within the configured tolerance. The first"
          + " run creates the baseline.")
  public void loginPageMatchesBaseline() {
    LoginPage loginPage = new LoginPage().open(FrameworkConfig.current().require("ui.base.url"));

    VisualComparator.Result result =
        VisualComparator.fromConfig(FrameworkConfig.current())
            .compare(DriverManager.get(), "saucedemo-login");

    assertThat(result.status())
        .as(
            "login page differs from baseline by %.3f%%; diff image: %s",
            result.diffPercent(), result.diffImage())
        .isNotEqualTo(VisualComparator.Status.MISMATCH);
  }
}
