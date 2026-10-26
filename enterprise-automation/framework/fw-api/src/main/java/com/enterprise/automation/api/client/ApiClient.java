package com.enterprise.automation.api.client;

import com.enterprise.automation.api.auth.AuthProvider;
import com.enterprise.automation.api.logging.ApiLogFilter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Objects;

/**
 * Base client for JSON REST services. Each call to {@link #request()} builds a fresh specification,
 * so no RestAssured state is shared between calls. The client returns raw responses and never
 * asserts.
 */
public class ApiClient {

  private final String baseUrl;
  private final AuthProvider auth;

  /**
   * @param baseUrl base URI of the service, for example {@code https://api.example.test}
   * @param auth authentication strategy applied to every request, or {@link AuthProvider#NONE}
   */
  public ApiClient(String baseUrl, AuthProvider auth) {
    this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    this.auth = Objects.requireNonNull(auth, "auth");
  }

  /**
   * @return a fresh specification with base URI, JSON negotiation, logging and auth applied
   */
  public RequestSpecification request() {
    RequestSpecification built =
        new RequestSpecBuilder()
            .setBaseUri(baseUrl)
            .setContentType(ContentType.JSON)
            .setAccept("application/json")
            .addFilter(new ApiLogFilter())
            .build();
    // A bare built spec cannot send requests; wrapping it in given() initialises RestAssured's
    // request pipeline.
    RequestSpecification spec = RestAssured.given().spec(built);
    return auth.apply(spec);
  }

  public String baseUrl() {
    return baseUrl;
  }
}
