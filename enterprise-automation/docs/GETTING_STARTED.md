# Getting Started: New Joiners and Adopting the Framework

This guide takes you from zero to a running suite, then shows how to reuse the framework for another company's UI and API tests, run it in Docker, put it in CI/CD, and send results to stakeholders.

## Part 1: Understand the framework (30 minutes)

1. Read `docs/ARCHITECTURE.md`. Learn the layers: tests, then domain-steps, then pages and clients, then framework core.
2. Read `docs/CONTRIBUTING.md`. Learn the rules: tests never touch WebDriver or RequestSpecification, and pages and clients never assert.
3. Skim `docs/adr/README.md` to see why the choices were made.

### Module map

| You want to... | Look in |
|---|---|
| Change how config, secrets, or threads work | `framework/fw-core` |
| Change browsers, drivers, waits, or accessibility/visual checks | `framework/fw-ui` |
| Change HTTP calls, auth, or schema checks | `framework/fw-api` |
| Add a page object | `automation/ui-pages` |
| Add an API client or model | `automation/api-clients`, `automation/api-models` |
| Add a business flow | `automation/domain-steps` |
| Add a test | `tests/tests-api`, `tests/tests-ui` |
| Add environment settings | `tests/*/src/test/resources/config/<env>.properties` |

## Part 2: Set up your machine (15 minutes)

Prerequisites: Java 17+, Git, Docker (only for grid runs). Maven is provided by `./mvnw`.

```bash
git clone <repo-url>
cd enterprise-automation
./mvnw -B clean verify
```

A green build means your setup works. It runs unit tests, the layering rules, Checkstyle, and Spotless.

## Part 3: Run your first tests (10 minutes)

```bash
# API smoke tests against the demo API
./mvnw -B -pl tests/tests-api -am verify -Dgroups=smoke -Denv=dev

# UI tests, headless
./mvnw -B -pl tests/tests-ui -am verify -Dgroups=regression -Denv=dev -Dheadless=true

# Open the Allure report
./mvnw -B allure:serve
```

Every dimension is a flag, not a code change: `-Denv`, `-Dtarget`, `-Dbrowser`, `-Dheadless`, `-Dmock.profile`, `-Dgroups`, `-Dthreads`.

Try the "pass or fail" check: change one expected value in a test, rerun, and see the failure with its screenshot in Allure. Then revert.

## Part 4: Write your first test (30 minutes)

Follow this order. Each step is one file or one class.

1. **Model** (if the API is involved): add a POJO in `automation/api-models`.
2. **Client or page**: add a client method in `automation/api-clients`, or a page in `automation/ui-pages`. Return state only. No `assert` calls.
3. **Step**: add a business method in `automation/domain-steps` that combines the client or page calls.
4. **Test**: add a class in `tests/...` that:
   - extends the base class (`BaseApiTest` or `BaseUiTest`),
   - has a TestNG group (`smoke`, `regression`, `api`, `ui`, `e2e`, `a11y`, or `visual`),
   - has `@Epic`, `@Feature`, `@Story`, and `@Description`,
   - creates its own data and deletes it at the end.
5. Run `./mvnw -B -q spotless:apply` before you commit.

## Part 5: Adopt the framework for another company

Use this when a new client needs UI and API tests on the same structure.

### Step 1: Copy and rename

1. Copy the repository into the new company's repo. Keep the module layout.
2. Rename `com.enterprise.automation` and the `groupId` to the client's namespace in every `pom.xml` and Java package.
3. Remove the sample pages and tests that do not apply.

### Step 2: Point the config at their systems

1. In `tests/tests-api/src/test/resources/config/`, create `dev.properties`, `qa.properties`, `uat.properties`, and `prod-readonly.properties`. Set `api.base.url` for each.
2. Do the same for `tests/tests-ui/src/test/resources/config/` with `ui.base.url`.
3. Never put passwords or tokens in these files. Use secrets (Step 5).

### Step 3: Write the API clients and models

1. For each service, create one client in `automation/api-clients` and one set of models in `automation/api-models`.
2. Put the request and response schemas in `tests/*/src/test/resources/schemas/`. Validate them in tests.
3. For auth, pick the provider in `fw-api` that matches their system: OAuth2, JWT, API key, or session.

### Step 4: Write the UI pages

1. For each screen, create a page in `automation/ui-pages` that extends `BasePage`.
2. Use stable locators: `id`, `data-test`, or `data-testid`. Ask their developers to add them if they are missing.
3. Reuse the shared components in `fw-ui` (header, modal, table, date picker, toast) where they fit.

### Step 5: Secrets

Set secrets as environment variables. The framework reads `SECRET_<NAME>` values. For example, `SECRET_UI_PASSWORD` is read for `ui.password`.

- Locally: export the variables in your shell or a local `.env` that is git-ignored.
- In CI: store them as repository or organization secrets (Part 7).
- Values are masked in logs automatically.

### Step 6: Mocks (optional)

If their downstream services are not reachable from CI, use `-Dmock.profile=mock` or `hybrid`. Add WireMock stubs in `fw-mock` or `tests/*/src/test/resources/mocks/`.

## Part 6: Run in Docker

### Local Selenium Grid

```bash
docker compose -f infra/docker/docker-compose.grid.yml up -d
./mvnw -B -pl tests/tests-ui -am verify -Dgroups=regression -Denv=qa -Dtarget=docker-grid -Dheadless=true
docker compose -f infra/docker/docker-compose.grid.yml down
```

The grid URL defaults to `http://localhost:4444`. Override it with `-Dgrid.url=...`.

### Run the whole suite in a container

Build the runner image from `infra/docker/Dockerfile.runner`, then run it with the environment variables it needs:

```bash
docker build -f infra/docker/Dockerfile.runner -t qa-runner .
docker run --rm -e SECRET_UI_PASSWORD -e SECRET_API_PASSWORD qa-runner
```

The Docker and K8s files were syntax-checked only. Run them once in your own environment before you depend on them.

### Mocks and reports

- `infra/docker/docker-compose.mock.yml` starts WireMock and a database for offline runs.
- `infra/docker/docker-compose.allure.yml` serves the Allure report.

## Part 7: Put it in CI/CD

Workflow files live in `infra/ci/` (GitHub Actions, Jenkinsfile, Azure Pipelines). GitHub Actions only reads from `.github/workflows` at the repository root, so copy the files there.

| Workflow | When it runs | Purpose |
|---|---|---|
| `pr-smoke.yml` | Each pull request | Fast smoke tests |
| `nightly-regression.yml` | Nightly schedule | Sharded full regression |
| `cross-browser.yml` | On demand or nightly | Chrome, Firefox, Edge |
| `post-deploy.yml` | After a deploy | Smoke tests against the new environment |

Steps for each workflow:
1. Set `SECRET_*` values in the CI secret store.
2. Set the `env` and `target` inputs for the environment.
3. Upload `target/allure-results` as a build artifact so the Allure report can be generated later.
4. Run `./mvnw -B allure:serve` locally, or use `allure generate` in CI, to publish the report.

## Part 8: Send results to stakeholders

The framework does not send email. Slack, Teams, and SMTP notifications are not implemented (see ADR-0006). Use one of these approaches in your CI system:

1. **Email the report artifact.** Add a final CI step that runs `allure generate target/allure-results -o target/allure-report`, zips the folder, and attaches it to an email. Use an SMTP action your team approves, for example `dawidd6/action-send-mail` in GitHub Actions. Test it with a dummy address first. This action is not part of this repo and was not verified here.
2. **Email the summary.** Read the TestNG totals from `target/surefire-reports` and send a short pass/fail summary with a link to the CI run.
3. **Link instead of attach.** Upload the Allure report as a CI artifact, or publish it to GitHub Pages or an internal web server, and send stakeholders the link. This avoids large attachments and mail-size limits.

Recommended: send a summary email with the report link for everyone, and attach the zipped report only for the QA lead.

Set the recipient list in CI secrets, not in the repository.

## Part 9: Checklist before go-live

- [ ] `./mvnw -B clean verify` passes on the client's repo.
- [ ] Config files exist for every environment they use.
- [ ] All secrets are set in CI, and none are in the repo.
- [ ] A smoke run passes against their dev environment.
- [ ] Docker grid run passes with `-Dtarget=docker-grid`.
- [ ] CI runs on a test branch and the artifact is downloadable.
- [ ] The email or link reaches a test recipient.
- [ ] The RUNBOOK's known gaps are reviewed with the client.

## Getting help

- Run and debug problems: `docs/RUNBOOK.md`.
- Naming and review rules: `docs/CONTRIBUTING.md`.
- Design questions: `docs/adr/README.md`.
