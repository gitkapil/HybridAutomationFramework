package com.enterprise.automation.tests.ui.storefront;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.pages.saucedemo.InventoryPage;
import com.enterprise.automation.pages.saucedemo.LoginPage;
import com.enterprise.automation.steps.storefront.StorefrontSteps;
import com.enterprise.automation.tests.ui.BaseUiTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Storefront UI")
@Feature("Authentication")
@Test(groups = "ui")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class StorefrontLoginTest extends BaseUiTest {

  @Test(groups = {"ui", "smoke"})
  @Story("Valid login")
  @Description("A standard user logs in and lands on the product inventory.")
  public void standardUserCanLogIn() {
    InventoryPage inventory = new StorefrontSteps().loginAsStandardUser();

    assertThat(inventory.title()).isEqualTo("Products");
  }

  @Test(groups = {"ui", "regression"})
  @Story("Locked-out user")
  @Description(
      "A locked-out user is refused with an explanatory error and stays on the login page.")
  public void lockedOutUserSeesError() {
    LoginPage loginPage = new StorefrontSteps().attemptLockedOutLogin();

    assertThat(loginPage.errorMessage()).containsIgnoringCase("locked out");
  }
}
