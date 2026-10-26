package com.enterprise.automation.ui.visual;

import com.enterprise.automation.core.config.FrameworkConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Pixel-level visual regression. Compares a screenshot with a committed PNG baseline. A missing
 * baseline is created on the first run. A mismatch writes the actual image and a diff image
 * (differing pixels in red) to the output directory. Pure JDK, so no vendor service is needed.
 */
public final class VisualComparator {

  /** Outcome of one comparison. */
  public enum Status {
    /** No baseline existed, or updates were requested; the screenshot became the baseline. */
    BASELINE_CREATED,
    /** Difference is within the configured tolerance. */
    MATCH,
    /** Difference exceeds the configured tolerance, or the image sizes differ. */
    MISMATCH
  }

  /**
   * @param status comparison outcome
   * @param diffPercent percentage of pixels that differ, from 0 to 100
   * @param diffImage diff image path, or {@code null} unless a mismatch occurred
   */
  public record Result(Status status, double diffPercent, Path diffImage) {}

  private static final int RED = 0xFFFF0000;

  private final Path baselineDir;
  private final Path outputDir;
  private final double tolerancePercent;
  private final boolean updateBaselines;

  /**
   * @param baselineDir directory holding committed {@code <name>.png} baselines
   * @param outputDir directory for actual and diff images
   * @param tolerancePercent allowed percentage of differing pixels
   * @param updateBaselines overwrite baselines instead of comparing
   */
  public VisualComparator(
      Path baselineDir, Path outputDir, double tolerancePercent, boolean updateBaselines) {
    if (tolerancePercent < 0 || tolerancePercent > 100) {
      throw new IllegalArgumentException("tolerancePercent must be between 0 and 100");
    }
    this.baselineDir = baselineDir;
    this.outputDir = outputDir;
    this.tolerancePercent = tolerancePercent;
    this.updateBaselines = updateBaselines;
  }

  /**
   * Reads {@code visual.baseline.dir}, {@code visual.output.dir}, {@code visual.tolerance.percent}
   * and {@code visual.update} from configuration.
   *
   * @param config resolved configuration
   * @return a comparator for the current run
   */
  public static VisualComparator fromConfig(FrameworkConfig config) {
    return new VisualComparator(
        Path.of(config.find("visual.baseline.dir").orElse("src/test/resources/visual/baseline")),
        Path.of(config.find("visual.output.dir").orElse("target/visual")),
        Double.parseDouble(config.find("visual.tolerance.percent").orElse("0.5")),
        Boolean.parseBoolean(config.find("visual.update").orElse("false")));
  }

  /**
   * @param driver driver on the page to capture
   * @param name baseline name, used as the file name without extension
   * @return the comparison result
   */
  public Result compare(WebDriver driver, String name) {
    byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    return compare(png, name);
  }

  /**
   * @param actualPng screenshot bytes in PNG format
   * @param name baseline name, used as the file name without extension
   * @return the comparison result
   */
  public Result compare(byte[] actualPng, String name) {
    Path baseline = baselineDir.resolve(name + ".png");
    try {
      Files.createDirectories(outputDir);
      if (updateBaselines || !Files.exists(baseline)) {
        Files.createDirectories(baselineDir);
        Files.write(baseline, actualPng);
        return new Result(Status.BASELINE_CREATED, 0.0, null);
      }
      BufferedImage expected = ImageIO.read(baseline.toFile());
      BufferedImage actual = ImageIO.read(new ByteArrayInputStream(actualPng));
      Path actualOut = outputDir.resolve(name + "-actual.png");
      Path diffOut = outputDir.resolve(name + "-diff.png");
      if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
        Files.write(actualOut, actualPng);
        ImageIO.write(actual, "png", diffOut.toFile());
        return new Result(Status.MISMATCH, 100.0, diffOut);
      }
      double percent = differencePercent(expected, actual, diffOut);
      if (percent <= tolerancePercent) {
        return new Result(Status.MATCH, percent, null);
      }
      Files.write(actualOut, actualPng);
      return new Result(Status.MISMATCH, percent, diffOut);
    } catch (IOException e) {
      throw new UncheckedIOException("Visual comparison failed for " + name, e);
    }
  }

  /** Compares pixel by pixel, writes the diff image, and returns the percentage that differs. */
  private static double differencePercent(
      BufferedImage expected, BufferedImage actual, Path diffOut) throws IOException {
    BufferedImage diff =
        new BufferedImage(actual.getWidth(), actual.getHeight(), BufferedImage.TYPE_INT_ARGB);
    long differing = 0;
    for (int y = 0; y < actual.getHeight(); y++) {
      for (int x = 0; x < actual.getWidth(); x++) {
        int pixel = actual.getRGB(x, y);
        if (pixel == expected.getRGB(x, y)) {
          diff.setRGB(x, y, pixel);
        } else {
          differing++;
          diff.setRGB(x, y, RED);
        }
      }
    }
    ImageIO.write(diff, "png", diffOut.toFile());
    long total = (long) actual.getWidth() * actual.getHeight();
    return total == 0 ? 0.0 : 100.0 * differing / total;
  }
}
