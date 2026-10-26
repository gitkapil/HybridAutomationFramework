package com.enterprise.automation.ui.page;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.ui.actions.WebActions;
import com.enterprise.automation.ui.driver.DriverManager;
import java.time.Duration;

/**
 * Base for page objects. Binds to the current thread's driver and exposes {@link WebActions}.
 * Subclasses expose state and actions only; assertions belong in tests.
 */
public abstract class BasePage {

  /** Config key for the explicit-wait timeout in seconds. */
  public static final String TIMEOUT_KEY = "ui.timeout.seconds";

  private static final int DEFAULT_TIMEOUT_SECONDS = 10;

  /** Interactions with the current page, already bound to the thread's driver. */
  protected final WebActions actions;

  protected BasePage() {
    this.actions = new WebActions(DriverManager.get(), timeout());
  }

  private static Duration timeout() {
    int seconds =
        FrameworkConfig.current()
            .find(TIMEOUT_KEY)
            .map(Integer::parseInt)
            .orElse(DEFAULT_TIMEOUT_SECONDS);
    return Duration.ofSeconds(seconds);
  }
}
