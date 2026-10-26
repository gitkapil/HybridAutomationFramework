package com.enterprise.automation.reporting.listener;

import com.enterprise.automation.core.security.SecretMasker;
import com.enterprise.automation.ui.driver.DriverManager;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * On every failed attempt, attaches the failure stack trace and, when the thread has a live
 * browser, a screenshot and the page source. API tests have no browser and get only the stack
 * trace. Output is masked before attaching.
 */
public final class FailureArtifactListener implements ITestListener {

  private static final Logger LOG = LoggerFactory.getLogger(FailureArtifactListener.class);

  @Override
  public void onTestFailure(ITestResult result) {
    Allure.addAttachment("Failure stack trace", "text/plain", stackTrace(result.getThrowable()));
    WebDriver driver = currentDriver();
    if (driver == null) {
      return;
    }
    attachScreenshot(driver);
    attachPageSource(driver);
  }

  private static WebDriver currentDriver() {
    try {
      return DriverManager.get();
    } catch (IllegalStateException noBrowserOnThread) {
      return null;
    }
  }

  private static void attachScreenshot(WebDriver driver) {
    if (!(driver instanceof TakesScreenshot)) {
      return;
    }
    try {
      byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(png), "png");
    } catch (RuntimeException e) {
      LOG.warn("Could not capture screenshot: {}", e.getMessage());
    }
  }

  private static void attachPageSource(WebDriver driver) {
    try {
      Allure.addAttachment(
          "Page source", "text/html", SecretMasker.mask(driver.getPageSource()), "html");
    } catch (RuntimeException e) {
      LOG.warn("Could not capture page source: {}", e.getMessage());
    }
  }

  private static String stackTrace(Throwable throwable) {
    if (throwable == null) {
      return "No throwable recorded";
    }
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer));
    return SecretMasker.mask(writer.toString());
  }
}
