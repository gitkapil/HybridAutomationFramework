package com.enterprise.automation.tests.ui;

import com.enterprise.automation.core.context.TestContext;
import com.enterprise.automation.core.security.SecretMasker;
import com.enterprise.automation.ui.driver.DriverManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Shared UI test lifecycle. Each test method gets its own browser bound to the current thread,
 * which is quit after the test so parallel runs never share a driver.
 */
public abstract class BaseUiTest {

  @BeforeMethod(alwaysRun = true)
  public void startBrowser() {
    DriverManager.start();
  }

  @AfterMethod(alwaysRun = true)
  public void stopBrowser() {
    try {
      DriverManager.quit();
    } finally {
      TestContext.reset();
      SecretMasker.clear();
    }
  }
}
