package com.enterprise.automation.api.logging;

import com.enterprise.automation.core.security.SecretMasker;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs method, URI, status, latency and bodies of every request. Bodies pass through {@link
 * SecretMasker}. Headers are deliberately not logged, because they carry credentials.
 */
public final class ApiLogFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(ApiLogFilter.class);

  @Override
  public Response filter(
      FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec,
      FilterContext ctx) {
    Object rawBody = requestSpec.getBody();
    LOG.info(
        "--> {} {} body={}",
        requestSpec.getMethod(),
        requestSpec.getURI(),
        SecretMasker.mask(String.valueOf(rawBody)));

    Response response = ctx.next(requestSpec, responseSpec);

    LOG.info(
        "<-- {} in {} ms body={}",
        response.getStatusCode(),
        response.getTime(),
        SecretMasker.mask(response.getBody().asString()));
    return response;
  }
}
