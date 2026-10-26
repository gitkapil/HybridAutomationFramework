package com.enterprise.automation.mock.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Owns one embedded WireMock server on a dynamic port. Implements {@link AutoCloseable} so it can
 * be stopped with try-with-resources or a {@code @AfterSuite} hook. Stubs come from classpath
 * mappings or the Java DSL.
 */
public final class WireMockManager implements AutoCloseable {

  private final WireMockServer server;
  private final Path extractedDir;

  private WireMockManager(WireMockServer server, Path extractedDir) {
    this.server = server;
    this.extractedDir = extractedDir;
  }

  /**
   * Starts a server on a free port, loading stub mappings from {@code classpathRoot}.
   *
   * @param classpathRoot classpath directory holding {@code mappings/} and {@code __files/}, for
   *     example {@code mocks/booking}; may be {@code null} to start empty
   * @param extensions response transformers or other extensions to register
   * @return a started manager
   */
  public static WireMockManager start(String classpathRoot, Extension... extensions) {
    WireMockConfiguration config = WireMockConfiguration.options().dynamicPort();
    Path extracted = null;
    if (classpathRoot != null) {
      URL root = resourceUrl(classpathRoot);
      if (root != null && "jar".equals(root.getProtocol())) {
        // WireMock cannot list classpath files inside a jar, so mappings are copied to a temp
        // directory.
        extracted = extractFromJar(root, classpathRoot);
        config.usingFilesUnderDirectory(extracted.toString());
      } else {
        config.usingFilesUnderClasspath(classpathRoot);
      }
    }
    for (Extension extension : extensions) {
      config.extensions(Objects.requireNonNull(extension, "extension"));
    }
    WireMockServer server = new WireMockServer(config);
    server.start();
    return new WireMockManager(server, extracted);
  }

  private static URL resourceUrl(String classpathRoot) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = WireMockManager.class.getClassLoader();
    }
    return loader.getResource(classpathRoot);
  }

  private static Path extractFromJar(URL root, String classpathRoot) {
    String prefix = classpathRoot.endsWith("/") ? classpathRoot : classpathRoot + "/";
    try {
      Path target = Files.createTempDirectory("wiremock-" + classpathRoot.replace('/', '-') + "-");
      JarURLConnection connection = (JarURLConnection) root.openConnection();
      try (JarFile jar = connection.getJarFile()) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
            continue;
          }
          Path file = target.resolve(entry.getName().substring(prefix.length()));
          Files.createDirectories(file.getParent());
          try (InputStream in = jar.getInputStream(entry)) {
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }
      return target;
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot extract WireMock resources from " + root, e);
    }
  }

  /**
   * @return the server's base URL, for example {@code http://localhost:54321}
   */
  public String baseUrl() {
    return "http://localhost:" + server.port();
  }

  /**
   * @return the underlying server for DSL stubbing and verification
   */
  public WireMockServer server() {
    return server;
  }

  /**
   * @return {@code true} while the server is accepting requests
   */
  public boolean isRunning() {
    return server.isRunning();
  }

  @Override
  public void close() {
    server.stop();
    deleteExtracted();
  }

  private void deleteExtracted() {
    if (extractedDir == null) {
      return;
    }
    try (Stream<Path> walk = Files.walk(extractedDir)) {
      walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
    } catch (IOException e) {
      // Best-effort cleanup of a temp directory.
    }
  }
}
