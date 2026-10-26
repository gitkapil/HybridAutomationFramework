package com.enterprise.automation.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.automation.core.security.SecretMasker;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class CookieTokenAuthTest {

  @AfterMethod
  void clearMasker() {
    SecretMasker.clear();
  }

  @Test(groups = "unit")
  void tokenIsFetchedOnceUntilInvalidated() {
    AtomicInteger calls = new AtomicInteger();
    CookieTokenAuth auth = new CookieTokenAuth("token", () -> "tok-" + calls.incrementAndGet());

    auth.apply(io.restassured.RestAssured.given());
    auth.apply(io.restassured.RestAssured.given());
    assertThat(calls.get()).isEqualTo(1);

    auth.invalidate();
    auth.apply(io.restassured.RestAssured.given());
    assertThat(calls.get()).isEqualTo(2);
  }

  @Test(groups = "unit")
  void fetchedTokenIsRegisteredForMasking() {
    CookieTokenAuth auth = new CookieTokenAuth("token", () -> "super-secret-token");

    auth.apply(io.restassured.RestAssured.given());

    assertThat(SecretMasker.mask("value=super-secret-token")).doesNotContain("super-secret-token");
  }
}
