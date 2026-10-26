package com.enterprise.automation.data.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapters from classpath data files to TestNG {@code Object[][]} rows. Each row holds one element,
 * so a test method takes a single parameter.
 */
public final class DataProviders {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private DataProviders() {}

  /**
   * Reads a JSON array of objects and maps each element to {@code type}.
   *
   * @param resource classpath location, for example {@code testdata/booking-payloads.json}
   * @param type element type
   * @param <T> element type
   * @return one row per array element
   * @throws IllegalArgumentException if the resource is missing or not a JSON array
   */
  public static <T> Object[][] fromJsonArray(String resource, Class<T> type) {
    List<T> items = readJson(resource, new TypeReference<List<T>>() {}.getType(), type);
    Object[][] rows = new Object[items.size()][1];
    for (int i = 0; i < items.size(); i++) {
      rows[i][0] = items.get(i);
    }
    return rows;
  }

  /**
   * Reads a CSV file whose first line is a header. Each data row becomes a map keyed by header
   * name. Values are trimmed. Blank lines are ignored. Commas inside quoted fields are not
   * supported.
   *
   * @param resource classpath location
   * @return one row per data line, each holding a {@code Map<String, String>}
   * @throws IllegalArgumentException if the resource is missing or empty
   */
  public static Object[][] fromCsv(String resource) {
    List<String> lines = readLines(resource);
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("CSV resource is empty: " + resource);
    }
    String[] headers = split(lines.get(0));
    List<Object[]> rows = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      if (line.isBlank()) {
        continue;
      }
      String[] values = split(line);
      Map<String, String> row = new LinkedHashMap<>();
      for (int i = 0; i < headers.length; i++) {
        row.put(headers[i], i < values.length ? values[i] : "");
      }
      rows.add(new Object[] {row});
    }
    return rows.toArray(new Object[0][]);
  }

  private static <T> List<T> readJson(
      String resource, java.lang.reflect.Type listType, Class<T> type) {
    try (InputStream in = open(resource)) {
      List<T> items = MAPPER.readValue(in, MAPPER.getTypeFactory().constructType(listType));
      if (items == null) {
        throw new IllegalArgumentException("Expected a JSON array in " + resource);
      }
      return items;
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Cannot read JSON data " + resource + " as " + type.getSimpleName(), e);
    }
  }

  private static List<String> readLines(String resource) {
    try (InputStream in = open(resource)) {
      String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return text.lines().toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String[] split(String line) {
    String[] parts = line.split(",", -1);
    for (int i = 0; i < parts.length; i++) {
      parts[i] = parts[i].trim();
    }
    return parts;
  }

  private static InputStream open(String resource) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = DataProviders.class.getClassLoader();
    }
    InputStream in = loader.getResourceAsStream(resource);
    if (in == null) {
      throw new IllegalArgumentException("Test data resource not found on classpath: " + resource);
    }
    return in;
  }
}
