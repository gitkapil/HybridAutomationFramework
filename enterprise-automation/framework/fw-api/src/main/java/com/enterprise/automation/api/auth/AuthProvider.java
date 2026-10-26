package com.enterprise.automation.api.auth;

import io.restassured.specification.RequestSpecification;

/** Strategy for attaching credentials to a request. Implementations must not perform assertions. */
@FunctionalInterface
public interface AuthProvider {

  /** Leaves the request unchanged. */
  AuthProvider NONE = spec -> spec;

  /**
   * @param spec the request specification being built
   * @return the same specification, with credentials applied
   */
  RequestSpecification apply(RequestSpecification spec);
}
