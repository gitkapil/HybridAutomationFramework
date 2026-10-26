package com.enterprise.automation.reporting.retry;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Applies the retry analyzer to every test and disables tests in the {@code quarantine} group
 * unless {@code -Dquarantine.include=true} is set. Quarantined tests still run in the nightly job
 * that sets it.
 */
public final class RetryAndQuarantineTransformer implements IAnnotationTransformer {

  public static final String QUARANTINE_GROUP = "quarantine";

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    annotation.setRetryAnalyzer(RetryAnalyzer.class);
    if (isQuarantined(annotation) && !includeQuarantined()) {
      annotation.setEnabled(false);
    }
  }

  private static boolean isQuarantined(ITestAnnotation annotation) {
    return annotation.getGroups() != null
        && Arrays.asList(annotation.getGroups()).contains(QUARANTINE_GROUP);
  }

  private static boolean includeQuarantined() {
    return FrameworkConfig.current()
        .find("quarantine.include")
        .map(Boolean::parseBoolean)
        .orElse(false);
  }
}
