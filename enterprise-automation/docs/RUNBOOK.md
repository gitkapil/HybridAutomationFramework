# Runbook

## Build

```bash
./mvnw -B clean verify
```

## Run tests

| Goal | Command |
|---|---|
| API smoke | `./mvnw -B -pl tests/tests-api -am verify -Dgroups=smoke -Denv=dev` |
| UI regression | `./mvnw -B -pl tests/tests-ui -am verify -Dgroups=regression -Denv=dev` |
| Accessibility | `./mvnw -B -pl tests/tests-ui -am verify -Dgroups=a11y -Denv=dev` |
| Visual | `./mvnw -B -pl tests/tests-ui -am verify -Dgroups=visual -Denv=dev` |
| Parallel | add `-Dthreads=4` |
| Headless | add `-Dheadless=true` |
| Mock downstream | add `-Dmock.profile=mock` |
| Docker Grid | add `-Dtarget=docker-grid` (or `k8s-grid` for K8s) |
| BrowserStack | add `-Dtarget=cloud` and set `SECRET_BROWSERSTACK_USER` and `SECRET_BROWSERSTACK_KEY` |

Use `-Dbrowser=chrome|firefox|edge` to choose the browser.

## Reports

```bash
./mvnw -B allure:serve
```

Failure attachments (screenshot, page source, console log) appear on failed tests.

## Visual baselines

- The first run creates `tests/tests-ui/src/test/resources/visual/baseline/<name>.png`.
- Commit the baseline. Later runs compare against it. Actual and diff images go to the output directory.
- To accept a deliberate UI change, delete the baseline file and rerun.

## Triage

1. Open the Allure report and find the failed test.
2. Check the attachments: screenshot, page source, and console log.
3. For API failures, check the request and response log.
4. Retried tests that pass on retry are reported as flaky. Check whether they are quarantined.
5. For a11y failures, read the axe rule ID and `helpUrl` in the assertion message.
6. For visual failures, open the `-diff.png` file.

## Debugging locally

- Run one test: `-Dtest=ClassName#method`.
- Run headed: `-Dheadless=false`.
- Set a breakpoint in the IDE and run the test class with TestNG.

## Known gaps

- Pact contract tests, Jira-Xray, and Slack or Teams notifications are not implemented.
- The BrowserStack path has been checked for configuration only, because live credentials are needed.
- Docker and K8s Grid manifests are syntax-checked only. Confirm them in your environment before relying on them.
- SonarQube needs a server and has not been run.
