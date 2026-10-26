package com.enterprise.automation.ui.a11y;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable result of an accessibility scan.
 *
 * @param violations every rule that failed on the page
 */
public record AccessibilityReport(List<Violation> violations) {

  /** Copies the list so the report cannot change after creation. */
  public AccessibilityReport {
    violations = List.copyOf(violations);
  }

  /**
   * @param impacts impact levels to keep, e.g. {@code "critical"}, {@code "serious"}
   * @return violations whose impact is one of the given levels
   */
  public List<Violation> withImpact(String... impacts) {
    Set<String> wanted = Set.of(impacts);
    return violations.stream()
        .filter(v -> wanted.contains(v.impact()))
        .collect(Collectors.toList());
  }

  /**
   * One failed axe-core rule.
   *
   * @param id axe rule id
   * @param impact {@code minor}, {@code moderate}, {@code serious} or {@code critical}
   * @param description rule description
   * @param helpUrl documentation link for the rule
   * @param nodeCount number of page elements that failed the rule
   */
  public record Violation(
      String id, String impact, String description, String helpUrl, int nodeCount) {}
}
