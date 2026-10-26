package com.enterprise.automation.ui.driver;

import com.enterprise.automation.core.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;

/**
 * Strategy for creating a {@link WebDriver} for one execution target. Selected by {@code -Dtarget}.
 */
public interface DriverProvider {

  /**
   * @param config resolved framework configuration (browser, headless flag, grid URL)
   * @return a new, unshared driver instance owned by the caller
   */
  WebDriver create(FrameworkConfig config);
}
