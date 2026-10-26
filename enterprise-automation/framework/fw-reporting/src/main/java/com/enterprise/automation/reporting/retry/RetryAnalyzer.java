package com.enterprise.automation.reporting.retry;

import com.enterprise.automation.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@code retry.count} times (default 0). TestNG creates one analyzer
 * per test method, so the attempt counter is never shared across threads.
 */
public final class RetryAnalyzer implements IRetryAnalyzer {

  private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

  private final int maxRetries;
  private int attempts;

  public RetryAnalyzer() {
    this.maxRetries =
        FrameworkConfig.current().find("retry.count").map(Integer::parseInt).orElse(0);
  }

  @Override
  public boolean retry(ITestResult result) {
    if (attempts < maxRetries) {
      attempts++;
      LOG.warn("Retrying {} (attempt {} of {})", result.getName(), attempts, maxRetries);
      return true;
    }
    return false;
  }
}
