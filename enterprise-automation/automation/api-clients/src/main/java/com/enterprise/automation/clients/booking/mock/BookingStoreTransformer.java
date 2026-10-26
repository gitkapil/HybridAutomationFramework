package com.enterprise.automation.clients.booking.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stateful in-memory store behind the mocked restful-booker {@code /booking} endpoints. Stub
 * mappings route requests here by name ({@code booking-store}); this class performs the create,
 * read and delete semantics. Each WireMock server owns one instance, so separate runs never share
 * bookings.
 */
public final class BookingStoreTransformer implements ResponseDefinitionTransformerV2 {

  /** Name referenced from the JSON stub mappings. */
  public static final String NAME = "booking-store";

  private static final String BOOKING_PATH = "/booking";
  private static final String JSON = "application/json";

  private final Map<Integer, String> bookings = new ConcurrentHashMap<>();
  private final AtomicInteger sequence = new AtomicInteger(1000);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ResponseDefinition transform(ServeEvent serveEvent) {
    Request request = serveEvent.getRequest();
    String path = request.getUrl();

    if (RequestMethod.POST.equals(request.getMethod()) && BOOKING_PATH.equals(path)) {
      return create(request.getBodyAsString());
    }
    if (path.startsWith(BOOKING_PATH + "/")) {
      return withId(request, path.substring(BOOKING_PATH.length() + 1));
    }
    return text(404, "Not Found");
  }

  private ResponseDefinition create(String body) {
    int id = sequence.incrementAndGet();
    bookings.put(id, body);
    return json(200, "{\"bookingid\":" + id + ",\"booking\":" + body + "}");
  }

  private ResponseDefinition withId(Request request, String idPart) {
    int id;
    try {
      id = Integer.parseInt(idPart);
    } catch (NumberFormatException e) {
      return text(404, "Not Found");
    }
    String stored = bookings.get(id);
    if (stored == null) {
      return text(404, "Not Found");
    }
    if (RequestMethod.GET.equals(request.getMethod())) {
      return json(200, stored);
    }
    if (RequestMethod.DELETE.equals(request.getMethod())) {
      bookings.remove(id);
      return text(201, "Created");
    }
    return text(405, "Method Not Allowed");
  }

  private static ResponseDefinition json(int status, String body) {
    return aResponse().withStatus(status).withHeader("Content-Type", JSON).withBody(body).build();
  }

  private static ResponseDefinition text(int status, String body) {
    return aResponse().withStatus(status).withBody(body).build();
  }

  @Override
  public boolean applyGlobally() {
    return false;
  }
}
