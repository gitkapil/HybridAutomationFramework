package com.enterprise.automation.clients.auth;

import com.enterprise.automation.api.auth.AuthProvider;
import com.enterprise.automation.api.client.ApiClient;
import com.enterprise.automation.models.auth.AuthRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/** Client for the {@code /auth} endpoint that issues session tokens. */
public class AuthClient {

  private final ApiClient api;

  /**
   * @param baseUrl base URI of the service
   */
  public AuthClient(String baseUrl) {
    this.api = new ApiClient(baseUrl, AuthProvider.NONE);
  }

  /**
   * @param username account name
   * @param password account password
   * @return the raw response of {@code POST /auth}
   */
  @Step("Request auth token")
  public Response createToken(String username, String password) {
    return api.request().body(new AuthRequest(username, password)).post("/auth");
  }
}
