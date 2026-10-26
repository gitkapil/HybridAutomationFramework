package com.enterprise.automation.api.auth;

import com.enterprise.automation.core.security.SecretMasker;
import com.enterprise.automation.core.util.Strings;
import io.restassured.specification.RequestSpecification;
import java.util.function.Supplier;

/**
 * Sends a session token as a cookie. The token is fetched lazily, registered with {@link
 * SecretMasker}, and reused until {@link #invalidate()} is called. Instances are not shared between
 * threads.
 */
public final class CookieTokenAuth implements AuthProvider {

  private final String cookieName;
  private final Supplier<String> tokenSupplier;
  private String cachedToken;

  /**
   * @param cookieName name of the cookie that carries the token
   * @param tokenSupplier fetches a fresh token; called at most once until invalidated
   */
  public CookieTokenAuth(String cookieName, Supplier<String> tokenSupplier) {
    this.cookieName = Strings.requireNonBlank(cookieName, "cookieName");
    this.tokenSupplier = tokenSupplier;
  }

  @Override
  public RequestSpecification apply(RequestSpecification spec) {
    return spec.cookie(cookieName, token());
  }

  /** Drops the cached token so the next request fetches a new one. */
  public void invalidate() {
    cachedToken = null;
  }

  private String token() {
    if (cachedToken == null) {
      cachedToken = Strings.requireNonBlank(tokenSupplier.get(), "token");
      SecretMasker.register(cachedToken);
    }
    return cachedToken;
  }
}
