package com.enterprise.automation.mock.fault;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds high-priority fault stubs to a running WireMock server so a test can exercise error handling
 * without changing production code. Priority 1 overrides the default mappings for the same URL.
 * Call {@link #clear} in teardown.
 */
public final class FaultInjector {

  private final WireMock wireMock;
  private final List<StubMapping> injected = new ArrayList<>();

  /**
   * @param wireMock client bound to the server under test
   */
  public FaultInjector(WireMock wireMock) {
    this.wireMock = wireMock;
  }

  /**
   * @param path URL path to fail, for example {@code /booking/1}
   * @param status HTTP status to return
   * @return the registered stub
   */
  public StubMapping status(String path, int status) {
    return register(
        WireMock.any(urlPathEqualTo(path))
            .atPriority(1)
            .willReturn(aResponse().withStatus(status)));
  }

  private StubMapping register(MappingBuilder builder) {
    StubMapping stub = wireMock.register(builder);
    injected.add(stub);
    return stub;
  }

  /**
   * @param path URL path to delay
   * @param delayMs response delay in milliseconds
   * @param status status to return after the delay
   * @return the registered stub
   */
  public StubMapping delay(String path, int delayMs, int status) {
    return register(
        WireMock.any(urlPathEqualTo(path))
            .atPriority(1)
            .willReturn(aResponse().withStatus(status).withFixedDelay(delayMs)));
  }

  /**
   * @param path URL path on which the connection is reset without a response
   * @return the registered stub
   */
  public StubMapping connectionReset(String path) {
    return register(
        WireMock.any(urlPathEqualTo(path))
            .atPriority(1)
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
  }

  /** Removes all stubs that this injector would have added. Other stubs are untouched. */
  public void clear() {
    for (StubMapping stub : injected) {
      wireMock.removeStubMapping(stub);
    }
    injected.clear();
  }
}
