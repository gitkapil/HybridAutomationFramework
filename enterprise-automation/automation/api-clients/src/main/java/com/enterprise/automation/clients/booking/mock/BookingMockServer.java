package com.enterprise.automation.clients.booking.mock;

import com.enterprise.automation.mock.wiremock.WireMockManager;

/**
 * JVM-wide WireMock stand-in for restful-booker. Started lazily on first use and stopped on JVM
 * shutdown, so every test in the run shares one stateful store.
 */
public final class BookingMockServer {

  private static final String MAPPINGS_ROOT = "mocks/booking";
  private static volatile WireMockManager instance;

  private BookingMockServer() {}

  /**
   * @return the running mock server, started on first call
   */
  public static synchronized WireMockManager get() {
    if (instance == null || !instance.isRunning()) {
      instance = WireMockManager.start(MAPPINGS_ROOT, new BookingStoreTransformer());
      Runtime.getRuntime().addShutdownHook(new Thread(instance::close));
    }
    return instance;
  }
}
