package com.enterprise.automation.data.db;

import com.enterprise.automation.core.config.FrameworkConfig;
import com.enterprise.automation.core.secrets.EnvSecretsProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Minimal JDBC helper for arranging and verifying data directly in a database. Each call opens and
 * closes its own connection, so instances are safe to share across threads.
 */
public final class JdbcHelper {

  private final String url;
  private final String username;
  private final String password;

  /**
   * @param url JDBC URL
   * @param username database user
   * @param password database password, never logged
   */
  public JdbcHelper(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * Builds a helper from configuration. Reads {@code <prefix>.jdbc.url} and {@code
   * <prefix>.jdbc.username}, and the password from the secrets provider under {@code
   * <prefix>.jdbc.password}.
   *
   * @param prefix configuration prefix, for example {@code db}
   * @return a configured helper
   */
  public static JdbcHelper fromConfig(String prefix) {
    FrameworkConfig config = FrameworkConfig.current();
    return new JdbcHelper(
        config.require(prefix + ".jdbc.url"),
        config.require(prefix + ".jdbc.username"),
        new EnvSecretsProvider().require(prefix + ".jdbc.password"));
  }

  /**
   * Executes an INSERT, UPDATE or DELETE statement.
   *
   * @param sql SQL with {@code ?} placeholders
   * @param params positional parameters
   * @return the number of affected rows
   */
  public int update(String sql, Object... params) {
    try (Connection connection = connect();
        PreparedStatement statement = prepare(connection, sql, params)) {
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new IllegalStateException("Update failed: " + sql, e);
    }
  }

  /**
   * Runs a query and returns the first column of the first row.
   *
   * @param sql SQL with {@code ?} placeholders
   * @param params positional parameters
   * @return the integer value
   * @throws IllegalStateException if the query returns no rows or the value is NULL
   */
  public int queryForInt(String sql, Object... params) {
    try (Connection connection = connect();
        PreparedStatement statement = prepare(connection, sql, params);
        ResultSet rs = statement.executeQuery()) {
      if (!rs.next()) {
        throw new IllegalStateException("Query returned no rows: " + sql);
      }
      int value = rs.getInt(1);
      if (rs.wasNull()) {
        throw new IllegalStateException("Query returned NULL: " + sql);
      }
      return value;
    } catch (SQLException e) {
      throw new IllegalStateException("Query failed: " + sql, e);
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  private static PreparedStatement prepare(Connection connection, String sql, Object... params)
      throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    for (int i = 0; i < params.length; i++) {
      statement.setObject(i + 1, params[i]);
    }
    return statement;
  }
}
