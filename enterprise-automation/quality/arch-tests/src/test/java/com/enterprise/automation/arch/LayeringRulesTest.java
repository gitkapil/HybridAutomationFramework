package com.enterprise.automation.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.testng.Assert.assertTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Enforces the layering principles: Tests -> Business flows -> Page objects / API clients -> Core.
 *
 * <p>Classes are imported from the compiled output of each module, so run this after the other
 * modules have been built (the reactor order already guarantees this for {@code mvn verify}).
 */
@Test(groups = "unit")
public class LayeringRulesTest {

  private static final String SELENIUM = "org.openqa.selenium..";
  private static final String RESTASSURED_REQUEST = "io.restassured.specification..";
  private static final String RESTASSURED_BUILDER = "io.restassured.builder..";
  private static final String RESTASSURED_ENTRY = "io.restassured.RestAssured";

  private static final String TESTS = "com.enterprise.automation.tests..";
  private static final String PAGES = "com.enterprise.automation.pages..";
  private static final String CLIENTS = "com.enterprise.automation.clients..";
  private static final String CORE = "com.enterprise.automation.core..";

  private static JavaClasses classes;

  @BeforeClass
  public static void importClasses() {
    Path root = Paths.get("../..").normalize();
    List<Path> locations =
        List.of(
            root.resolve("framework/fw-core/target/classes"),
            root.resolve("framework/fw-ui/target/classes"),
            root.resolve("framework/fw-api/target/classes"),
            root.resolve("framework/fw-data/target/classes"),
            root.resolve("framework/fw-mock/target/classes"),
            root.resolve("framework/fw-reporting/target/classes"),
            root.resolve("automation/ui-pages/target/classes"),
            root.resolve("automation/api-clients/target/classes"),
            root.resolve("automation/api-models/target/classes"),
            root.resolve("automation/domain-steps/target/classes"),
            root.resolve("tests/tests-api/target/test-classes"),
            root.resolve("tests/tests-ui/target/test-classes"));
    classes = new ClassFileImporter().importPaths(locations);
  }

  @Test(description = "Sanity check: the rules must not pass vacuously on an empty classpath.")
  public void classesWereImported() {
    assertTrue(
        classes.size() > 50,
        "Expected compiled classes, found "
            + classes.size()
            + ". Build the reactor first (mvn verify).");
  }

  @Test(description = "Tests must never touch WebDriver directly.")
  public void testsMustNotUseWebDriver() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(TESTS)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(SELENIUM)
            .because("tests interact with the browser through domain steps and page objects");
    rule.check(classes);
  }

  @Test(description = "Tests must never build or touch RestAssured request specifications.")
  public void testsMustNotUseRequestSpecifications() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(TESTS)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(RESTASSURED_REQUEST, RESTASSURED_BUILDER)
            .because("requests are built by API clients, not by tests");
    rule.check(classes);
  }

  @Test(description = "Tests must not call the RestAssured entry point directly.")
  public void testsMustNotCallRestAssuredEntryPoint() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(TESTS)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName(RESTASSURED_ENTRY)
            .because("the RestAssured static entry point belongs to fw-api and the API clients");
    rule.check(classes);
  }

  @Test(description = "Page objects must not contain assertions.")
  public void pagesMustNotAssertWithAssertJ() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(PAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.assertj.core..")
            .because("page objects expose state; tests assert");
    rule.check(classes);
  }

  @Test(description = "Page objects must not contain TestNG assertions.")
  public void pagesMustNotAssertWithTestNg() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(PAGES)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("org.testng.Assert")
            .because("page objects expose state; tests assert");
    rule.check(classes);
  }

  @Test(description = "API clients must not contain assertions.")
  public void clientsMustNotAssertWithAssertJ() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(CLIENTS)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.assertj.core..")
            .because("API clients expose responses; tests assert");
    rule.check(classes);
  }

  @Test(description = "API clients must not contain TestNG assertions.")
  public void clientsMustNotAssertWithTestNg() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(CLIENTS)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("org.testng.Assert")
            .because("API clients expose responses; tests assert");
    rule.check(classes);
  }

  @Test(description = "fw-core must not depend on Selenium or RestAssured.")
  public void coreMustNotDependOnDriverOrHttpLibraries() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(CORE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(SELENIUM, "io.restassured..")
            .because("fw-core is the lowest layer and must stay library-agnostic");
    rule.check(classes);
  }

  @Test(description = "fw-core must not depend on any higher framework module.")
  public void coreMustNotDependOnOtherFrameworkModules() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(CORE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "com.enterprise.automation.ui..",
                "com.enterprise.automation.api..",
                "com.enterprise.automation.data..",
                "com.enterprise.automation.mock..",
                "com.enterprise.automation.reporting..",
                PAGES,
                CLIENTS,
                TESTS)
            .because("dependencies point downward only");
    rule.check(classes);
  }
}
