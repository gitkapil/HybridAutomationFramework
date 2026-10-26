package com.enterprise.automation.steps.booking;

import com.enterprise.automation.clients.booking.BookingClient;
import com.enterprise.automation.data.fixture.Fixture;
import com.enterprise.automation.models.booking.Booking;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/**
 * API-only business flows for bookings. Used to arrange and clean up test data without touching the
 * UI.
 */
public final class BookingSteps {

  private final BookingClient client;

  /**
   * @param client booking API client
   */
  public BookingSteps(BookingClient client) {
    this.client = client;
  }

  /**
   * Creates a booking and returns a fixture whose teardown deletes it. Closing the fixture is
   * guaranteed by the caller's try-with-resources or registry.
   *
   * @param booking payload to create
   * @return a fixture holding the new booking id
   * @throws IllegalStateException if creation does not return HTTP 200 with a booking id
   */
  @Step("Arrange booking")
  public Fixture<Integer> arrangeBooking(Booking booking) {
    Response created = client.create(booking);
    if (created.statusCode() != 200) {
      throw new IllegalStateException("Booking arrange failed: HTTP " + created.statusCode());
    }
    int id = created.jsonPath().getInt("bookingid");
    return Fixture.of(id, this::deleteBooking);
  }

  /**
   * @param id booking identifier
   * @return the raw response of fetching the booking
   */
  @Step("Fetch booking {id}")
  public Response fetch(int id) {
    return client.get(id);
  }

  private void deleteBooking(int id) {
    Response deleted = client.delete(id);
    if (deleted.statusCode() != 201) {
      throw new IllegalStateException(
          "Booking teardown failed for id " + id + ": HTTP " + deleted.statusCode());
    }
  }
}
