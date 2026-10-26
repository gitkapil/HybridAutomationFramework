package com.enterprise.automation.tests.ui.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.clients.booking.BookingClient;
import com.enterprise.automation.data.fixture.Fixture;
import com.enterprise.automation.models.booking.Booking;
import com.enterprise.automation.models.booking.BookingBuilder;
import com.enterprise.automation.pages.saucedemo.InventoryPage;
import com.enterprise.automation.steps.booking.BookingSteps;
import com.enterprise.automation.steps.storefront.StorefrontSteps;
import com.enterprise.automation.tests.ui.BaseUiTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Hybrid E2E flow: API arrange, UI act, API verify, API cleanup. The UI leg runs against the
 * storefront and does not depend on the booking data. This demonstrates orchestration across
 * layers, not a business link between the two systems. The booking is held by a {@link Fixture} so
 * cleanup runs even if an assertion fails.
 */
@Epic("Hybrid E2E")
@Feature("Booking arranged by API, storefront verified by UI")
@Test(groups = "e2e")
public class StorefrontBookingHybridTest extends BaseUiTest {

  private BookingSteps bookingSteps;

  @BeforeClass(alwaysRun = true)
  void setUp() {
    bookingSteps = new BookingSteps(BookingClient.fromConfig());
  }

  @Test(groups = {"e2e", "smoke"})
  @Story("API arrange, UI act, API verify, API cleanup")
  public void apiArrangedBookingIsVerifiedAroundUiCartFlow() {
    Booking payload = BookingBuilder.aBooking().build();

    try (Fixture<Integer> booking = bookingSteps.arrangeBooking(payload)) {
      InventoryPage inventory = new StorefrontSteps().loginAsStandardUser();
      inventory.addBackpackToCart();
      assertThat(inventory.isBackpackInCart()).as("cart badge shows the item").isTrue();

      Response fetched = bookingSteps.fetch(booking.value());
      assertThat(fetched.statusCode()).isEqualTo(200);
      assertThat(fetched.as(Booking.class)).isEqualTo(payload);
    }
  }
}
