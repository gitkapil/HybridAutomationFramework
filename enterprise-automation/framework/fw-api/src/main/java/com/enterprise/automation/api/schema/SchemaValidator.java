package com.enterprise.automation.api.schema;

import com.enterprise.automation.api.json.JsonMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Validates JSON documents against a JSON Schema (draft-07) loaded from the classpath. The
 * validator returns violation messages and never asserts; tests decide what counts as a failure.
 */
public final class SchemaValidator {

  private static final ObjectMapper MAPPER = JsonMapperFactory.create();

  private final JsonSchema schema;

  private SchemaValidator(JsonSchema schema) {
    this.schema = schema;
  }

  /**
   * @param resourcePath classpath location of the schema, for example {@code
   *     schemas/booking.schema.json}
   * @return a validator for that schema
   * @throws IllegalArgumentException if the resource is missing or unreadable
   */
  public static SchemaValidator fromClasspath(String resourcePath) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = loader.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Schema not found on classpath: " + resourcePath);
      }
      JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(in);
      return new SchemaValidator(schema);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read schema: " + resourcePath, e);
    }
  }

  /**
   * @param json a JSON document
   * @return sorted violation messages; empty when the document is valid
   * @throws IllegalArgumentException if the input is not well-formed JSON
   */
  public List<String> validate(String json) {
    try {
      return validate(MAPPER.readTree(json));
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Input is not well-formed JSON", e);
    }
  }

  /**
   * @param node a parsed JSON document
   * @return sorted violation messages; empty when the document is valid
   */
  public List<String> validate(JsonNode node) {
    return schema.validate(node).stream().map(ValidationMessage::getMessage).sorted().toList();
  }
}
