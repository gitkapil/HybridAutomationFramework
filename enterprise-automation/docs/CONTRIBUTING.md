# Contributing

## Naming

- Packages are lowercase and grouped by layer and domain, for example `com.enterprise.automation.pages.saucedemo`.
- Page objects end in `Page`, components in `Component`, API clients in `Client`, models in plain nouns.
- Tests end in `Test` and live in the matching `tests-*` module.

## Locators

- Prefer `id`, `data-test`, or stable attributes. Avoid XPath based on position.
- Keep locators in the page object as `private static final By` fields.

## Tagging

- Every test has one TestNG group from `smoke`, `regression`, `api`, `ui`, `e2e`, `a11y`, or `visual`.
- Every test has `@Epic`, `@Feature`, and `@Story`, and steps use `@Step`.
- Run a group with `-Dgroups=<name>`.

## Layering rules

- Tests call steps and assert. They never touch WebDriver or RequestSpecification.
- Page objects and API clients expose state only. No assertions.
- Use the API to arrange and clean up data. Use the UI only for what is under test.
- Use `Awaitility` or explicit waits. `Thread.sleep` is forbidden.
- Never commit secrets. Use `SECRET_*` environment variables.

## Review checklist

- [ ] `./mvnw -B clean verify` passes, including Spotless, Checkstyle, and ArchUnit.
- [ ] New tests have a group, Allure annotations, and their own data with cleanup.
- [ ] No new Selenium or RestAssured imports in the wrong layer.
- [ ] No assertions in pages or clients.
- [ ] New config keys have defaults in `framework-defaults.properties`.
- [ ] Docs updated where behavior changed.

## Formatting

Run `./mvnw -B -q spotless:apply` before committing. The build fails if formatting is wrong.
