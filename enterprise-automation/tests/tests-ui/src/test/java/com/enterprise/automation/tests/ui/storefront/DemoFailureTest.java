package com.enterprise.automation.tests.ui.storefront;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.pages.saucedemo.InventoryPage;
import com.enterprise.automation.steps.storefront.StorefrontSteps;
import com.enterprise.automation.tests.ui.BaseUiTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * Deliberately failing tests that demonstrate failure artifacts (screenshot, page source, stack
 * trace) and retry/quarantine behaviour. Grouped separately so they never run in the normal {@code
 * ui} suite.
 */
@Epic("Framework demo")
@Feature("Failure artifacts")
@Test(groups = "demo-failure")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class DemoFailureTest extends BaseUiTest {

  @Test(groups = "demo-failure")
  @Story("Intentional failure")
  @Description("Fails on purpose so the report shows a screenshot, page source and stack trace.")
  public void intentionalFailureAttachesArtifacts() {
    InventoryPage inventory = new StorefrontSteps().loginAsStandardUser();

    assertThat(inventory.title()).isEqualTo("Deliberately Wrong Title");
  }

  @Test(groups = {"demo-failure", "quarantine"})
  @Story("Quarantined failure")
  @Description("Quarantined failing test. Skipped unless -Dquarantine.include=true.")
  public void quarantinedFailureIsSkippedByDefault() {
    assertThat(false).as("quarantined demo failure").isTrue();
  }
}
