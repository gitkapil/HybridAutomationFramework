package com.enterprise.automation.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Per-thread test execution context. Each thread owns one instance, so parallel tests never share
 * state.
 *
 * <p>The correlation ID is published to SLF4J's MDC under {@link #MDC_KEY}, so it appears in every
 * log line produced by the owning thread. Call {@link #reset()} at the start or end of each test
 * method, for example from a listener, so that a pooled thread does not carry state from a previous
 * test. Instances are not safe for concurrent access from multiple threads by design.
 */
public final class TestContext {

  /** MDC key for the correlation ID. */
  public static final String MDC_KEY = "correlationId";

  private static final ThreadLocal<TestContext> HOLDER = ThreadLocal.withInitial(TestContext::new);

  private final String correlationId;
  private final Map<String, Object> attributes = new HashMap<>();

  private TestContext() {
    this.correlationId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put(MDC_KEY, correlationId);
  }

  /**
   * @return the context bound to the current thread, created on first access
   */
  public static TestContext current() {
    return HOLDER.get();
  }

  /**
   * Discards the current thread's context and clears the MDC entry. The next {@link #current()}
   * call creates a fresh context with a new correlation ID.
   */
  public static void reset() {
    HOLDER.remove();
    MDC.remove(MDC_KEY);
  }

  /**
   * @return the correlation ID of this context
   */
  public String correlationId() {
    return correlationId;
  }

  /**
   * Stores a value for the rest of this test.
   *
   * @param key attribute name
   * @param value attribute value
   * @return this context for chaining
   */
  public TestContext put(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  /**
   * @param key attribute name
   * @param type expected type
   * @param <T> expected type parameter
   * @return the attribute if present and of the expected type
   */
  public <T> Optional<T> get(String key, Class<T> type) {
    Object value = attributes.get(key);
    return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
  }
}
