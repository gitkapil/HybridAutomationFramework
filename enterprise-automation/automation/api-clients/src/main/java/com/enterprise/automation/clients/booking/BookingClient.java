package com.enterprise.automation.clients.booking;

import com.enterprise.automation.api.auth.AuthProvider;
import com.enterprise.automation.api.auth.CookieTokenAuth;
import com.enterprise.automation.api.client.ApiClient;
import com.enterprise.automation.clients.auth.AuthClient;
import com.enterprise.automation.clients.booking.mock.BookingMockServer;
import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.core.secrets.EnvSecretsProvider;
import com.enterprise.automation.mock.profile.MockProfiles;
import com.enterprise.automation.models.booking.Booking;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/**
 * Client for the restful-booker {@code /booking} resource. Mutating calls authenticate with a token
 * obtained from {@link AuthClient}. Every method returns the raw response for the test to assert
 * on.
 */
public class BookingClient {

  private static final String COOKIE_NAME = "token";
  private static final String SERVICE = "booking";
  private static final String MOCK_TOKEN = "mocktoken0123456";

  private final ApiClient api;

  /**
   * @param baseUrl base URI of the service
   * @param auth credentials applied to every request
   */
  public BookingClient(String baseUrl, AuthProvider auth) {
    this.api = new ApiClient(baseUrl, auth);
  }

  /**
   * Builds a client from configuration. The base URL comes from {@code api.base.url}, the user name
   * from {@code api.username}, and the password from the secrets provider ({@code
   * SECRET_API_PASSWORD}).
   *
   * @return a configured client
   */
  public static BookingClient fromConfig() {
    FrameworkConfig config = FrameworkConfig.current();
    if (MockProfiles.isMocked(SERVICE)) {
      String mockUrl = BookingMockServer.get().baseUrl();
      return new BookingClient(mockUrl, new CookieTokenAuth(COOKIE_NAME, () -> MOCK_TOKEN));
    }
    String baseUrl = config.require("api.base.url");
    String username = config.require("api.username");
    String password = new EnvSecretsProvider().require("api.password");

    AuthClient authClient = new AuthClient(baseUrl);
    CookieTokenAuth auth =
        new CookieTokenAuth(
            COOKIE_NAME,
            () -> authClient.createToken(username, password).jsonPath().getString("token"));
    return new BookingClient(baseUrl, auth);
  }

  /**
   * @param booking booking payload
   * @return the raw response of {@code POST /booking}
   */
  @Step("Create booking")
  public Response create(Booking booking) {
    return api.request().body(booking).post("/booking");
  }

  /**
   * @param id booking identifier
   * @return the raw response of {@code GET /booking/{id}}
   */
  @Step("Get booking {id}")
  public Response get(int id) {
    return api.request().get("/booking/{id}", id);
  }

  /**
   * @param id booking identifier
   * @return the raw response of {@code DELETE /booking/{id}}
   */
  @Step("Delete booking {id}")
  public Response delete(int id) {
    return api.request().delete("/booking/{id}", id);
  }
}
