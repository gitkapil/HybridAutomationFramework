package com.enterprise.automation.core.secrets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.enterprise.automation.core.security.SecretMasker;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class EnvSecretsProviderTest {

  @AfterMethod(alwaysRun = true)
  public void clearMasker() {
    SecretMasker.clear();
  }

  @Test(description = "Logical name maps to SECRET_ prefixed variable")
  public void resolvesDottedNameToEnvVariable() {
    SecretsProvider provider = new EnvSecretsProvider(Map.of("SECRET_API_TOKEN", "tok-123456"));

    assertThat(provider.require("api.token")).isEqualTo("tok-123456");
  }

  @Test(description = "Required secret is registered for masking")
  public void requiredSecretIsMasked() {
    SecretsProvider provider = new EnvSecretsProvider(Map.of("SECRET_DB_PASSWORD", "pw-987654"));

    provider.require("db.password");

    assertThat(SecretMasker.mask("password=pw-987654")).isEqualTo("password=" + SecretMasker.MASK);
  }

  @Test(description = "Missing secret fails without revealing other values")
  public void missingSecretFails() {
    SecretsProvider provider = new EnvSecretsProvider(Map.of());

    assertThatThrownBy(() -> provider.require("api.token"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Secret not found: api.token");
  }
}
