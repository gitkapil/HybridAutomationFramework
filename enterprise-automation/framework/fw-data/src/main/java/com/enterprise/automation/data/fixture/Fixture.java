package com.enterprise.automation.data.fixture;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A piece of test data that is torn down when closed. Typically created through the API in a test's
 * arrange phase and removed in a {@code try-with-resources} block or by a {@link FixtureRegistry}.
 *
 * @param <T> type of the fixture value
 */
public final class Fixture<T> implements AutoCloseable {

  private final T value;
  private final Consumer<T> teardown;
  private boolean closed;

  private Fixture(T value, Consumer<T> teardown) {
    this.value = Objects.requireNonNull(value, "fixture value");
    this.teardown = Objects.requireNonNull(teardown, "teardown");
  }

  /**
   * @param value the created test data
   * @param teardown action that removes the data; invoked at most once
   * @param <T> value type
   * @return a new fixture
   */
  public static <T> Fixture<T> of(T value, Consumer<T> teardown) {
    return new Fixture<>(value, teardown);
  }

  /**
   * @return the fixture value
   */
  public T value() {
    return value;
  }

  /** Runs the teardown once. Subsequent calls are no-ops. */
  @Override
  public void close() {
    if (!closed) {
      closed = true;
      teardown.accept(value);
    }
  }
}
