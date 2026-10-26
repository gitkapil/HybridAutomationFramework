# Architecture

## Overview

The framework is a multi-module Maven build (Java 17, TestNG, Selenium 4, RestAssured, WireMock, Allure). Dependencies point downward only:

```
tests/*  ->  automation/domain-steps  ->  automation/ui-pages, automation/api-clients  ->  framework/*
```

## Modules

| Module | Responsibility | Must not depend on |
|---|---|---|
| `framework/fw-core` | Layered config, secrets, `TestContext` (ThreadLocal), correlation-ID logging with masking, retry, utils | Selenium, RestAssured |
| `framework/fw-api` | `ApiClient`, auth providers with token cache, schema validation, logging filter | Selenium |
| `framework/fw-ui` | `DriverFactory` with local, docker-grid, k8s-grid, cloud (BrowserStack) providers; `WebActions`; waits; axe-core accessibility; pixel visual comparison | RestAssured |
| `framework/fw-data` | Builders, fixtures with guaranteed teardown, DataProvider adapters, JDBC helper, PII masking | - |
| `framework/fw-mock` | WireMock manager; `MockProfile` = REAL / MOCK / HYBRID per downstream service | - |
| `framework/fw-reporting` | Allure integration, failure-artifact and retry listeners | - |
| `automation/api-models` | Request and response POJOs | - |
| `automation/api-clients` | Service clients, no assertions | WebDriver |
| `automation/ui-pages` | Page objects and components, no assertions | RestAssured |
| `automation/domain-steps` | Business flows per domain (`api/`, `ui/`, `hybrid/`) | - |
| `tests/tests-*` | TestNG tests; they call only steps and assert | WebDriver and RequestSpecification directly |
| `quality/arch-tests` | ArchUnit rules that enforce the layering | - |

## Execution model

Every execution dimension is configuration, not code:

- Config precedence (lowest to highest): `framework-defaults.properties`, then `config/<env>.properties`, then `-D` system properties, then `FW_*` environment variables.
- Secrets come from `SecretsProvider`. The default `EnvSecretsProvider` maps logical name `browserstack.key` to `SECRET_BROWSERSTACK_KEY`. Values are registered with `SecretMasker` so they never appear in logs.
- Threads: `TestContext` and the WebDriver are ThreadLocal, so `-Dthreads=N` runs without shared mutable state.

## Key flows

- **UI test:** test to `BaseUiTest` (starts `DriverManager` per method) to steps to page objects to `WebActions` to WebDriver.
- **API test:** test to steps to client to `ApiClient` to RestAssured. Tests arrange and clean up data through the API.
- **Hybrid:** API arrange, UI act, API verify, API cleanup.
- **Failure:** `FailureArtifactListener` attaches a screenshot, page source, and console log to Allure.
- **Accessibility:** `AccessibilityChecker` injects axe-core 4.11.3 and returns a report. The test asserts on it.
- **Visual:** `VisualComparator` pixel-diffs against `tests/tests-ui/src/test/resources/visual/baseline/`. The first run creates the baseline.

## Deferred

Pact contract tests, Jira-Xray, Slack, and Teams notifications are not implemented. See ADR-0006.
