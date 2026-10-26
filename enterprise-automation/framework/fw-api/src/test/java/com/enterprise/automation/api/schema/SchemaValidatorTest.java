package com.enterprise.automation.api.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorTest {

  private static final String SCHEMA = "schemas/test-item.schema.json";

  @Test(groups = "unit")
  void validDocumentProducesNoViolations() {
    SchemaValidator validator = SchemaValidator.fromClasspath(SCHEMA);

    List<String> violations = validator.validate("{\"id\": 1, \"name\": \"widget\"}");

    assertThat(violations).isEmpty();
  }

  @Test(groups = "unit")
  void missingRequiredFieldIsReported() {
    SchemaValidator validator = SchemaValidator.fromClasspath(SCHEMA);

    List<String> violations = validator.validate("{\"id\": 1}");

    assertThat(violations).isNotEmpty();
    assertThat(String.join("\n", violations)).contains("name");
  }

  @Test(groups = "unit")
  void missingSchemaFailsFast() {
    assertThatThrownBy(() -> SchemaValidator.fromClasspath("schemas/does-not-exist.json"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does-not-exist");
  }

  @Test(groups = "unit")
  void malformedJsonFailsFast() {
    SchemaValidator validator = SchemaValidator.fromClasspath(SCHEMA);

    assertThatThrownBy(() -> validator.validate("{not json"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
