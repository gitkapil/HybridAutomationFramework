package com.enterprise.automation.core.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class TestContextTest {

  @AfterMethod(alwaysRun = true)
  public void cleanUp() {
    TestContext.reset();
  }

  @Test(description = "Same thread gets the same context instance")
  public void sameThreadReturnsSameInstance() {
    assertThat(TestContext.current()).isSameAs(TestContext.current());
  }

  @Test(description = "Correlation ID is published to MDC for log correlation")
  public void correlationIdPublishedToMdc() {
    String id = TestContext.current().correlationId();

    assertThat(MDC.get(TestContext.MDC_KEY)).isEqualTo(id);
  }

  @Test(description = "Reset creates a fresh context with a new correlation ID")
  public void resetCreatesNewContext() {
    TestContext first = TestContext.current();
    TestContext.current().put("order", "42");
    TestContext.reset();

    TestContext second = TestContext.current();

    assertThat(second).isNotSameAs(first);
    assertThat(second.correlationId()).isNotEqualTo(first.correlationId());
    assertThat(second.get("order", String.class)).isEmpty();
  }

  @Test(description = "Each thread sees only its own context")
  public void contextIsIsolatedPerThread() throws InterruptedException {
    TestContext.current().put("owner", "main");
    AtomicReference<String> seenByWorker = new AtomicReference<>();
    AtomicReference<String> workerId = new AtomicReference<>();

    Thread worker =
        new Thread(
            () -> {
              TestContext context = TestContext.current();
              context.put("owner", "worker");
              seenByWorker.set(context.get("owner", String.class).orElse("none"));
              workerId.set(context.correlationId());
              TestContext.reset();
            });
    worker.start();
    worker.join();

    assertThat(seenByWorker.get()).isEqualTo("worker");
    assertThat(workerId.get()).isNotEqualTo(TestContext.current().correlationId());
    assertThat(TestContext.current().get("owner", String.class)).contains("main");
  }

  @Test(description = "Typed attribute lookup returns empty for a mismatched type")
  public void typedLookupRejectsWrongType() {
    TestContext.current().put("count", 7);

    assertThat(TestContext.current().get("count", String.class)).isEmpty();
    assertThat(TestContext.current().get("count", Integer.class)).contains(7);
  }
}
