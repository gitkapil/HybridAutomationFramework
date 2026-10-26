package com.enterprise.automation.data.fixture;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Collects fixtures created during one test and closes them in reverse order of registration, so
 * dependent data is removed before the data it depends on. Every teardown is attempted even if an
 * earlier one fails.
 *
 * <p>An instance is not shared between threads. Create one per test method.
 */
public final class FixtureRegistry implements AutoCloseable {

  private final Deque<AutoCloseable> fixtures = new ArrayDeque<>();

  /**
   * @param fixture fixture to tear down when this registry closes
   * @param <F> fixture type
   * @return the same fixture, for inline use
   */
  public <F extends AutoCloseable> F register(F fixture) {
    fixtures.push(fixture);
    return fixture;
  }

  /**
   * @return number of fixtures still awaiting teardown
   */
  public int size() {
    return fixtures.size();
  }

  /**
   * Closes all registered fixtures, last registered first.
   *
   * @throws IllegalStateException if one or more teardowns failed; the first failure is the cause
   *     and the rest are suppressed
   */
  @Override
  public void close() {
    Throwable first = null;
    while (!fixtures.isEmpty()) {
      AutoCloseable fixture = fixtures.pop();
      try {
        fixture.close();
      } catch (Throwable t) {
        if (first == null) {
          first = t;
        } else {
          first.addSuppressed(t);
        }
      }
    }
    if (first != null) {
      throw new IllegalStateException("Fixture teardown failed", first);
    }
  }
}
