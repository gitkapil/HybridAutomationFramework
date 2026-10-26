package com.enterprise.automation.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.enterprise.automation.data.db.JdbcHelper;
import com.enterprise.automation.data.faker.PiiMasker;
import com.enterprise.automation.data.fixture.Fixture;
import com.enterprise.automation.data.fixture.FixtureRegistry;
import com.enterprise.automation.data.provider.DataProviders;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class DataModuleTest {

  @Test
  public void registryClosesFixturesInReverseOrder() {
    StringBuilder order = new StringBuilder();
    try (FixtureRegistry registry = new FixtureRegistry()) {
      registry.register(Fixture.of("parent", v -> order.append(v).append(';')));
      registry.register(Fixture.of("child", v -> order.append(v).append(';')));
      assertThat(registry.size()).isEqualTo(2);
    }
    assertThat(order.toString()).isEqualTo("child;parent;");
  }

  @Test
  public void registryAttemptsEveryTeardownAndReportsFailure() {
    AtomicInteger closed = new AtomicInteger();
    FixtureRegistry registry = new FixtureRegistry();
    registry.register(Fixture.of("a", v -> closed.incrementAndGet()));
    registry.register(
        Fixture.of(
            "b",
            v -> {
              throw new IllegalStateException("boom " + v);
            }));
    registry.register(Fixture.of("c", v -> closed.incrementAndGet()));

    assertThatThrownBy(registry::close)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Fixture teardown failed")
        .hasCauseInstanceOf(IllegalStateException.class);
    assertThat(closed.get()).isEqualTo(2);
  }

  @Test
  public void fixtureTeardownRunsOnlyOnce() {
    AtomicInteger calls = new AtomicInteger();
    Fixture<String> fixture = Fixture.of("x", v -> calls.incrementAndGet());
    fixture.close();
    fixture.close();
    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  public void csvProviderMapsRowsByHeader() {
    Object[][] rows = DataProviders.fromCsv("testdata/sample.csv");

    assertThat(rows).hasDimensions(2, 1);
    @SuppressWarnings("unchecked")
    Map<String, String> first = (Map<String, String>) rows[0][0];
    assertThat(first).containsEntry("id", "7").containsEntry("name", "alpha");
  }

  @Test
  public void jsonProviderMapsArrayElements() {
    Object[][] rows = DataProviders.fromJsonArray("testdata/sample.json", Map.class);

    assertThat(rows).hasDimensions(2, 1);
    @SuppressWarnings("unchecked")
    Map<String, Object> second = (Map<String, Object>) rows[1][0];
    assertThat(second).containsEntry("name", "beta");
  }

  @Test
  public void missingResourceFailsFast() {
    assertThatThrownBy(() -> DataProviders.fromCsv("testdata/none.csv"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
  }

  @Test
  public void jdbcHelperRoundTripsAgainstInMemoryDatabase() {
    JdbcHelper db = new JdbcHelper("jdbc:h2:mem:data_test;DB_CLOSE_DELAY=-1", "sa", "");
    db.update("CREATE TABLE IF NOT EXISTS item (id INT PRIMARY KEY, qty INT)");
    db.update("INSERT INTO item (id, qty) VALUES (?, ?)", 1, 42);

    assertThat(db.queryForInt("SELECT qty FROM item WHERE id = ?", 1)).isEqualTo(42);
    assertThat(db.update("DELETE FROM item WHERE id = ?", 1)).isEqualTo(1);
    assertThatThrownBy(() -> db.queryForInt("SELECT qty FROM item WHERE id = ?", 1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no rows");
  }

  @Test
  public void piiMaskerKeepsOnlyShape() {
    assertThat(PiiMasker.maskEmail("alice@example.com")).isEqualTo("a***@example.com");
    assertThat(PiiMasker.maskEmail("not-an-email")).isEqualTo("***");
    assertThat(PiiMasker.maskEmail(null)).isNull();
  }
}
