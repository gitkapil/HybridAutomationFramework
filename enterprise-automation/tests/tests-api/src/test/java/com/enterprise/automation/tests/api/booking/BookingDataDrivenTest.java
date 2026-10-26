package com.enterprise.automation.tests.api.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.clients.booking.BookingClient;
import com.enterprise.automation.data.fixture.Fixture;
import com.enterprise.automation.data.provider.DataProviders;
import com.enterprise.automation.models.booking.Booking;
import com.enterprise.automation.models.booking.BookingBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import java.util.Map;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Data-driven booking tests. Payloads come from JSON; unknown ids come from CSV. Each created
 * booking is held by a {@link Fixture}, so it is deleted even when an assertion fails.
 */
@Epic("Booking API")
@Feature("Booking data-driven")
@Test(groups = "api")
public class BookingDataDrivenTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private BookingClient client;

  @BeforeClass(alwaysRun = true)
  void setUp() {
    client = BookingClient.fromConfig();
  }

  @DataProvider(name = "payloads")
  public Object[][] payloads() {
    return DataProviders.fromJsonArray("testdata/booking-payloads.json", Booking.class);
  }

  @DataProvider(name = "unknownIds")
  public Object[][] unknownIds() {
    return DataProviders.fromCsv("testdata/unknown-booking-ids.csv");
  }

  @Test(
      dataProvider = "payloads",
      groups = {"api", "regression"})
  @Story("Create and retrieve from data file")
  public void createdBookingMatchesDataFilePayload(Map<String, Object> row) {
    Booking payload = MAPPER.convertValue(row, Booking.class);
    try (Fixture<Integer> booking = arrange(payload)) {
      Response fetched = client.get(booking.value());

      assertThat(fetched.statusCode()).isEqualTo(200);
      assertThat(fetched.as(Booking.class)).isEqualTo(payload);
    }
  }

  @Test(
      dataProvider = "unknownIds",
      groups = {"api", "regression"})
  @Story("Unknown booking ids are rejected")
  public void unknownBookingIdReturnsExpectedStatus(Map<String, String> row) {
    Response response = client.get(parseId(row.get("id")));

    assertThat(response.statusCode()).isEqualTo(Integer.parseInt(row.get("expectedStatus")));
  }

  @Test(groups = {"api", "regression"})
  @Story("Builder produces unique payloads")
  public void builderProducesUniqueGuestNames() {
    Booking first = BookingBuilder.aBooking().build();
    Booking second = BookingBuilder.aBooking().build();

    assertThat(first.lastname()).isNotEqualTo(second.lastname());
  }

  private Fixture<Integer> arrange(Booking payload) {
    Response created = client.create(payload);
    assertThat(created.statusCode()).as("arrange status").isEqualTo(200);
    int id = created.jsonPath().getInt("bookingid");
    return Fixture.of(id, this::deleteBooking);
  }

  private void deleteBooking(int id) {
    Response deleted = client.delete(id);
    assertThat(deleted.statusCode()).as("teardown status for " + id).isEqualTo(201);
  }

  private static int parseId(String raw) {
    try {
      return Integer.parseInt(raw);
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
