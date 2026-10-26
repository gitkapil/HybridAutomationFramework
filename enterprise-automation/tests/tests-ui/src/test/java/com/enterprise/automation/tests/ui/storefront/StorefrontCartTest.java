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

@Epic("Storefront UI")
@Feature("Cart")
@Test(groups = "ui")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class StorefrontCartTest extends BaseUiTest {

  @Test(groups = {"ui", "regression"})
  @Story("Add to cart updates badge")
  @Description(
      "Adding an item from the inventory updates the cart badge to 1 and shows the remove control.")
  public void addingItemUpdatesCartBadge() {
    InventoryPage inventory = new StorefrontSteps().loginAsStandardUser();

    inventory.addBackpackToCart();

    assertThat(inventory.cartCount()).isEqualTo(1);
    assertThat(inventory.isBackpackInCart()).isTrue();
  }
}
