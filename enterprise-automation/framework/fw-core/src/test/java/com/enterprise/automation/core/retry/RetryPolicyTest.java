package com.enterprise.automation.core.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class RetryPolicyTest {

  @Test(description = "Returns the first success after transient failures")
  public void succeedsAfterTransientFailures() {
    AtomicInteger calls = new AtomicInteger();
    RetryPolicy policy = RetryPolicy.builder().maxAttempts(3).build();

    String result =
        policy.execute(
            () -> {
              if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("transient");
              }
              return "ok";
            });

    assertThat(result).isEqualTo("ok");
    assertThat(calls).hasValue(3);
  }

  @Test(description = "Rethrows the last runtime failure once attempts are exhausted")
  public void exhaustsAttempts() {
    AtomicInteger calls = new AtomicInteger();
    RetryPolicy policy = RetryPolicy.builder().maxAttempts(2).build();

    assertThatThrownBy(
            () ->
                policy.execute(
                    () -> {
                      calls.incrementAndGet();
                      throw new IllegalStateException("always");
                    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("always");

    assertThat(calls).hasValue(2);
  }

  @Test(description = "Non-retryable failures stop immediately")
  public void nonRetryableFailsImmediately() {
    AtomicInteger calls = new AtomicInteger();
    RetryPolicy policy =
        RetryPolicy.builder().maxAttempts(5).retryOn(t -> t instanceof IOException).build();

    assertThatThrownBy(
            () ->
                policy.execute(
                    () -> {
                      calls.incrementAndGet();
                      throw new IllegalArgumentException("bad input");
                    }))
        .isInstanceOf(IllegalArgumentException.class);

    assertThat(calls).hasValue(1);
  }

  @Test(description = "Checked exceptions are wrapped in RetryException after exhaustion")
  public void checkedExceptionWrapped() {
    RetryPolicy policy = RetryPolicy.builder().maxAttempts(2).build();

    assertThatThrownBy(
            () ->
                policy.execute(
                    () -> {
                      throw new IOException("disk");
                    }))
        .isInstanceOf(RetryPolicy.RetryException.class)
        .hasMessage("Action failed after 2 attempt(s)")
        .hasCauseInstanceOf(IOException.class);
  }

  @Test(description = "maxAttempts below one is rejected")
  public void rejectsInvalidAttempts() {
    assertThatThrownBy(() -> RetryPolicy.builder().maxAttempts(0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
