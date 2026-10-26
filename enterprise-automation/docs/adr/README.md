# Architecture Decision Records

## ADR-0001: Multi-module Maven with downward-only layering

**Status:** Accepted

**Context:** Tests, business flows, page objects, API clients, and core utilities need clear boundaries so that UI and API concerns do not leak into each other.

**Decision:** Use a multi-module Maven build. Enforce layering with ArchUnit rules in `quality/arch-tests`. Tests may not use WebDriver or RequestSpecification directly, page objects and clients may not assert, and `fw-core` may not depend on Selenium or RestAssured.

**Consequences:** Violations fail the build. Rule breakage was verified by a deliberate violation.

## ADR-0002: TestNG with ThreadLocal state

**Status:** Accepted

**Context:** Parallel execution must work from day one, and drivers must never leak between threads.

**Decision:** Use TestNG for groups, DataProviders, and parallelism. Hold `TestContext` and `DriverManager` in ThreadLocal.

**Consequences:** No shared mutable test state. `-Dthreads` is a config setting.

## ADR-0003: Typed layered config with a hand-rolled loader

**Status:** Accepted, with deviation

**Context:** The brief asked for Owner or Typesafe Config.

**Decision:** Implement a small loader in `fw-core` with the precedence defaults < env file < system properties < env vars. This avoids an extra dependency for a four-level precedence rule.

**Consequences:** Typed accessors are hand-written. Owner or Typesafe can replace the loader later without changing callers.

## ADR-0004: Pure-JDK visual comparison and axe-core for accessibility

**Status:** Accepted, with deviation

**Context:** Applitools and Percy need paid accounts and external services.

**Decision:** Use a pixel-diff comparator built on `java.awt` with a configurable tolerance, and run axe-core from the `org.webjars.npm:axe-core` WebJar. Pin axe-core to 4.11.3, because 4.10.2 is not on Maven Central.

**Consequences:** No external service is needed. Baselines are per browser and per environment.

## ADR-0005: Cloud and grid as target providers

**Status:** Accepted

**Context:** Local, Docker Grid, K8s Grid, and BrowserStack must be switchable without code changes.

**Decision:** Implement a `DriverProvider` per target and select it with `-Dtarget`. Credentials come from secrets, never from the repository.

**Consequences:** BrowserStack was verified only for configuration, because live credentials were not available.

## ADR-0006: Deferred integrations

**Status:** Accepted

**Context:** Pact, Jira-Xray, Slack, and Teams need external accounts or endpoints.

**Decision:** Defer Pact and Jira-Xray. Slack and Teams are not yet implemented. Record this as an open item.

**Consequences:** The brief's full scope is not met yet, and these items are listed in the RUNBOOK.
