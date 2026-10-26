package com.enterprise.automation.core.retry;

import com.enterprise.automation.core.security.SecretMasker;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable retry policy that re-runs an action on retryable failures.
 *
 * <p>There is deliberately no back-off sleep. Callers that need to wait between attempts should
 * wait on an observable condition with Awaitility, not sleep. Runtime exceptions are rethrown
 * unchanged. Checked exceptions are wrapped in {@link RetryException}.
 */
public final class RetryPolicy {

  private static final Logger LOG = LoggerFactory.getLogger(RetryPolicy.class);

  private final int maxAttempts;
  private final Predicate<Throwable> retryable;

  private RetryPolicy(Builder builder) {
    this.maxAttempts = builder.maxAttempts;
    this.retryable = builder.retryable;
  }

  /**
   * @return a new builder with a single attempt and retry-on-everything
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Runs the action, retrying on retryable failures until attempts are exhausted.
   *
   * @param action the action to run
   * @param <T> result type
   * @return the first successful result
   * @throws RuntimeException the last runtime failure, or {@link RetryException} wrapping a checked
   *     failure
   */
  public <T> T execute(Callable<T> action) {
    Objects.requireNonNull(action, "action must not be null");
    for (int attempt = 1; ; attempt++) {
      try {
        return action.call();
      } catch (Exception e) {
        if (attempt >= maxAttempts || !retryable.test(e)) {
          throw toRuntime(e, attempt);
        }
        LOG.warn(
            "Attempt {}/{} failed, retrying: {}",
            attempt,
            maxAttempts,
            SecretMasker.mask(e.toString()));
      }
    }
  }

  private static RuntimeException toRuntime(Exception e, int attempts) {
    if (e instanceof RuntimeException runtime) {
      return runtime;
    }
    return new RetryException("Action failed after " + attempts + " attempt(s)", e);
  }

  /** Raised when a checked exception survives all retry attempts. */
  public static final class RetryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message description
     * @param cause the last failure
     */
    public RetryException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Fluent builder for {@link RetryPolicy}. */
  public static final class Builder {
    private int maxAttempts = 1;
    private Predicate<Throwable> retryable = t -> true;

    private Builder() {}

    /**
     * @param maxAttempts total attempts including the first call, must be at least 1
     * @return this builder
     */
    public Builder maxAttempts(int maxAttempts) {
      if (maxAttempts < 1) {
        throw new IllegalArgumentException("maxAttempts must be >= 1 but was " + maxAttempts);
      }
      this.maxAttempts = maxAttempts;
      return this;
    }

    /**
     * @param retryable predicate that decides whether a failure may be retried
     * @return this builder
     */
    public Builder retryOn(Predicate<Throwable> retryable) {
      this.retryable = Objects.requireNonNull(retryable, "retryable must not be null");
      return this;
    }

    /**
     * @return the immutable policy
     */
    public RetryPolicy build() {
      return new RetryPolicy(this);
    }
  }
}
