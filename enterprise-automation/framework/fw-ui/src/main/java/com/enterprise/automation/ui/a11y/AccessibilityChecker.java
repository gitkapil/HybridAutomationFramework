package com.enterprise.automation.ui.a11y;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Runs axe-core against the current page and returns the findings as data. Tests decide what counts
 * as a failure; this class never asserts. The axe-core script is bundled as a WebJar, so no network
 * access is needed at run time.
 */
public final class AccessibilityChecker {

  /**
   * axe-core version bundled as a WebJar; must match {@code axe-core.version} in the parent POM.
   */
  public static final String AXE_VERSION = "4.11.3";

  static final String AXE_RESOURCE =
      "/META-INF/resources/webjars/axe-core/" + AXE_VERSION + "/axe.min.js";

  private static final String RUN_SCRIPT =
      "var done = arguments[arguments.length - 1];"
          + "axe.run(document, {runOnly: {type: 'tag', values: ['wcag2a', 'wcag2aa']}})"
          + ".then(function (r) {"
          + "  done(r.violations.map(function (v) {"
          + "    return {id: v.id, impact: v.impact, description: v.description,"
          + "            helpUrl: v.helpUrl, nodeCount: v.nodes.length};"
          + "  }));"
          + "}).catch(function (e) { done({error: String(e && e.message)}); });";

  private final String axeScript;

  /** Loads the bundled axe-core script from the classpath. */
  public AccessibilityChecker() {
    this(loadAxeScript());
  }

  /**
   * @param axeScript the axe-core source to inject; injectable for tests
   */
  public AccessibilityChecker(String axeScript) {
    this.axeScript = axeScript;
  }

  /**
   * @param driver driver positioned on the page to analyse
   * @return the report for WCAG 2.0 A and AA rules
   * @throws IllegalStateException if axe-core reports an error while running
   */
  public AccessibilityReport analyze(WebDriver driver) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript(axeScript);
    Object result = js.executeAsyncScript(RUN_SCRIPT);
    return toReport(result);
  }

  private static AccessibilityReport toReport(Object result) {
    if (result instanceof Map<?, ?> error && error.containsKey("error")) {
      throw new IllegalStateException("axe-core failed: " + error.get("error"));
    }
    List<AccessibilityReport.Violation> violations = new ArrayList<>();
    if (result instanceof List<?> items) {
      for (Object item : items) {
        Map<?, ?> v = (Map<?, ?>) item;
        violations.add(
            new AccessibilityReport.Violation(
                String.valueOf(v.get("id")),
                String.valueOf(v.get("impact")),
                String.valueOf(v.get("description")),
                String.valueOf(v.get("helpUrl")),
                ((Number) v.get("nodeCount")).intValue()));
      }
    }
    return new AccessibilityReport(violations);
  }

  private static String loadAxeScript() {
    try (InputStream in = AccessibilityChecker.class.getResourceAsStream(AXE_RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("axe-core script not found on classpath: " + AXE_RESOURCE);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read axe-core script", e);
    }
  }
}
