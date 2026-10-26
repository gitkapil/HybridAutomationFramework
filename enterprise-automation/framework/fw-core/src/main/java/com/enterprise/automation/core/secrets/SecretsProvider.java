package com.enterprise.automation.core.secrets;

import com.enterprise.automation.core.security.SecretMasker;
import java.util.Optional;

/**
 * Source of secret values. Implementations must never log the values they return.
 *
 * <p>Every value returned through {@link #require(String)} is registered with {@link SecretMasker}
 * so that it is redacted from subsequent log output.
 */
public interface SecretsProvider {

  /**
   * @param name logical secret name, e.g. {@code api.token}
   * @return the secret if this provider knows it
   */
  Optional<String> find(String name);

  /**
   * @param name logical secret name
   * @return the secret value, registered for masking
   * @throws IllegalStateException if no provider in this source knows the secret
   */
  default String require(String name) {
    String value =
        find(name).orElseThrow(() -> new IllegalStateException("Secret not found: " + name));
    SecretMasker.register(value);
    return value;
  }
}
