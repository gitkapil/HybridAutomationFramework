package com.enterprise.automation.core.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class SecretMaskerTest {

  @AfterMethod(alwaysRun = true)
  public void clearSecrets() {
    SecretMasker.clear();
  }

  @Test(description = "Registered secrets are replaced in text")
  public void masksRegisteredSecret() {
    SecretMasker.register("s3cr3t-token");

    assertThat(SecretMasker.mask("Authorization: Bearer s3cr3t-token"))
        .isEqualTo("Authorization: Bearer " + SecretMasker.MASK);
  }

  @Test(description = "Overlapping secrets are fully redacted, longest first")
  public void longerSecretMaskedFirst() {
    SecretMasker.register("abc");
    SecretMasker.register("abcdef");

    assertThat(SecretMasker.mask("value=abcdef")).isEqualTo("value=" + SecretMasker.MASK);
  }

  @Test(description = "Null and blank registrations are ignored and null text is preserved")
  public void ignoresNullAndBlank() {
    SecretMasker.register(null);
    SecretMasker.register("   ");

    assertThat(SecretMasker.mask(null)).isNull();
    assertThat(SecretMasker.mask("plain text")).isEqualTo("plain text");
  }
}
