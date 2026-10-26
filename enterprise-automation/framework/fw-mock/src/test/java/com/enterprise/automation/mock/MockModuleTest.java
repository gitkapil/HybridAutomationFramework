package com.enterprise.automation.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.mock.fault.FaultInjector;
import com.enterprise.automation.mock.profile.MockProfile;
import com.enterprise.automation.mock.profile.MockProfiles;
import com.enterprise.automation.mock.wiremock.WireMockManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class MockModuleTest {

  private WireMockManager mock;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

  @BeforeMethod
  public void startMock() {
    mock = WireMockManager.start(null);
    WireMock.configureFor("localhost", mock.server().port());
  }

  @AfterMethod(alwaysRun = true)
  public void stopMock() {
    mock.close();
  }

  @Test
  public void profileParsingIsCaseInsensitiveAndRejectsUnknown() {
    assertThat(MockProfile.parse(" Hybrid ")).isEqualTo(MockProfile.HYBRID);
    assertThatThrownBy(() -> MockProfile.parse("staging"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("staging");
  }

  @Test
  public void globalProfileAppliesToEveryService() {
    FrameworkConfig config = FrameworkConfig.of(Map.of("mock.profile", "mock"));
    assertThat(MockProfiles.resolve("booking", config)).isEqualTo(MockProfile.MOCK);
  }

  @Test
  public void perServiceOverrideWinsOverGlobal() {
    FrameworkConfig config =
        FrameworkConfig.of(
            Map.of(
                "mock.profile", "mock",
                "mock.payments.profile", "real"));
    assertThat(MockProfiles.resolve("payments", config)).isEqualTo(MockProfile.REAL);
    assertThat(MockProfiles.resolve("booking", config)).isEqualTo(MockProfile.MOCK);
  }

  @Test
  public void hybridMocksOnlyListedServices() {
    FrameworkConfig config =
        FrameworkConfig.of(
            Map.of(
                "mock.profile", "hybrid",
                "mock.hybrid.services", "Booking, auth"));
    assertThat(MockProfiles.resolve("booking", config)).isEqualTo(MockProfile.MOCK);
    assertThat(MockProfiles.resolve("auth", config)).isEqualTo(MockProfile.MOCK);
    assertThat(MockProfiles.resolve("payments", config)).isEqualTo(MockProfile.REAL);
  }

  @Test
  public void realProfileIsTheDefault() {
    assertThat(MockProfiles.resolve("booking", FrameworkConfig.of(Map.of())))
        .isEqualTo(MockProfile.REAL);
  }

  @Test
  public void faultInjectorReturnsConfiguredStatusAndClears()
      throws IOException, InterruptedException {
    FaultInjector faults = new FaultInjector(new WireMock("localhost", mock.server().port()));
    faults.status("/boom", 503);

    assertThat(get(mock.baseUrl() + "/boom").statusCode()).isEqualTo(503);

    faults.clear();
    assertThat(get(mock.baseUrl() + "/boom").statusCode()).isNotEqualTo(503);
  }

  @Test
  public void faultInjectorAddsLatency() throws IOException, InterruptedException {
    FaultInjector faults = new FaultInjector(new WireMock("localhost", mock.server().port()));
    faults.delay("/slow", 300, 200);

    long start = System.nanoTime();
    HttpResponse<String> response = get(mock.baseUrl() + "/slow");
    long elapsedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(elapsedMs).isGreaterThanOrEqualTo(300);
  }

  @Test
  public void faultInjectorCanResetConnection() {
    new FaultInjector(new WireMock("localhost", mock.server().port())).connectionReset("/drop");

    assertThatThrownBy(() -> get(mock.baseUrl() + "/drop")).isInstanceOf(IOException.class);
  }

  private HttpResponse<String> get(String url) throws IOException, InterruptedException {
    return http.send(
        HttpRequest.newBuilder(URI.create(url)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
