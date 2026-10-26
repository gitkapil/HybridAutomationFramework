package com.enterprise.automation.pages.saucedemo;

import com.enterprise.automation.ui.page.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Page object for the saucedemo.com product inventory page, reached after a successful login. */
public final class InventoryPage extends BasePage {

  private static final By TITLE = By.cssSelector("[data-test='title']");
  private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
  private static final By BACKPACK_ADD_BUTTON = By.id("add-to-cart-sauce-labs-backpack");
  private static final By BACKPACK_REMOVE_BUTTON = By.id("remove-sauce-labs-backpack");

  /**
   * @return the page title text, for example "Products"
   */
  public String title() {
    return actions.text(TITLE);
  }

  /** Adds the Sauce Labs Backpack to the cart. */
  @Step("Add Sauce Labs Backpack to cart")
  public void addBackpackToCart() {
    actions.click(BACKPACK_ADD_BUTTON);
  }

  /**
   * @return {@code true} if the Backpack shows a remove button, meaning it is in the cart
   */
  public boolean isBackpackInCart() {
    return actions.isDisplayed(BACKPACK_REMOVE_BUTTON);
  }

  /**
   * @return the cart badge count, or 0 when the badge is not shown
   */
  public int cartCount() {
    if (!actions.isDisplayed(CART_BADGE)) {
      return 0;
    }
    return Integer.parseInt(actions.text(CART_BADGE).trim());
  }
}
