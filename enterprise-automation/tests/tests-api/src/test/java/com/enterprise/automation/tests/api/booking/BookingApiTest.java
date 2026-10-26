package com.enterprise.automation.tests.api.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.api.schema.SchemaValidator;
import com.enterprise.automation.clients.booking.BookingClient;
import com.enterprise.automation.core.context.TestContext;
import com.enterprise.automation.core.security.SecretMasker;
import com.enterprise.automation.models.booking.Booking;
import com.enterprise.automation.models.booking.BookingReference;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Epic("Booking API")
@Feature("Booking lifecycle")
@Test(groups = "api")
@org.testng.annotations.Listeners({
  io.qameta.allure.testng.AllureTestNg.class,
  com.enterprise.automation.reporting.listener.FailureArtifactListener.class
})
public class BookingApiTest {

  static final String BOOKING_ID_KEY = "bookingId";
  private static final int UNKNOWN_BOOKING_ID = 999_999_999;

  private BookingClient client;
  private SchemaValidator referenceSchema;

  @BeforeClass(alwaysRun = true)
  void setUp() {
    client = BookingClient.fromConfig();
    referenceSchema = SchemaValidator.fromClasspath("schemas/booking-reference.schema.json");
  }

  @AfterMethod(alwaysRun = true)
  void cleanUp() {
    try {
      TestContext.current().get(BOOKING_ID_KEY, Integer.class).ifPresent(id -> deleteQuietly(id));
    } finally {
      TestContext.reset();
      SecretMasker.clear();
    }
  }

  @Test(groups = {"api", "smoke"})
  @Story("Create then retrieve")
  public void createdBookingIsRetrievable() {
    Booking payload = BookingTestData.randomBooking();

    int id = createBooking(payload);
    Booking fetched = fetchBooking(id);

    assertThat(fetched).isEqualTo(payload);
  }

  @Test(groups = {"api", "regression"})
  @Story("Negative: unknown booking")
  public void unknownBookingReturns404() {
    Response response = getBooking(UNKNOWN_BOOKING_ID);

    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test(groups = {"api", "regression"})
  @Story("Schema: create response")
  public void createResponseMatchesSchema() {
    Response response = createRaw(BookingTestData.randomBooking());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(referenceSchema.validate(response.getBody().asString())).isEmpty();
  }

  @Step("Create booking and track it for cleanup")
  private int createBooking(Booking payload) {
    Response response = createRaw(payload);
    assertThat(response.statusCode()).isEqualTo(200);
    int id = response.as(BookingReference.class).bookingid();
    TestContext.current().put(BOOKING_ID_KEY, id);
    return id;
  }

  @Step("Fetch booking")
  private Booking fetchBooking(int id) {
    Response response = getBooking(id);
    assertThat(response.statusCode()).isEqualTo(200);
    return response.as(Booking.class);
  }

  private Response createRaw(Booking payload) {
    return client.create(payload);
  }

  private Response getBooking(int id) {
    return client.get(id);
  }

  private void deleteQuietly(int id) {
    Response response = client.delete(id);
    assertThat(response.statusCode()).as("cleanup of booking %s", id).isEqualTo(201);
  }
}
