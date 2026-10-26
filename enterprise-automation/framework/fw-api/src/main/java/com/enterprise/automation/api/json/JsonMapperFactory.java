package com.enterprise.automation.api.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Factory for the framework's Jackson configuration. Unknown properties are tolerated so that tests
 * keep working when services add fields, and nulls are omitted from request bodies.
 */
public final class JsonMapperFactory {

  private JsonMapperFactory() {}

  /**
   * @return a newly configured {@link ObjectMapper}
   */
  public static ObjectMapper create() {
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
