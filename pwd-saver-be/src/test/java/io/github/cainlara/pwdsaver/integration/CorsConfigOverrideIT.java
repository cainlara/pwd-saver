package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Proves the allowed-origins allow-list is driven entirely by the
 * {@code cors.allowed-origins} property (SC-003) rather than hardcoded: with
 * it overridden to a different origin than {@code application-test.yml}'s
 * default, only the overridden origin is granted access.
 */
@TestPropertySource(properties = "cors.allowed-origins=http://other.example.com")
class CorsConfigOverrideIT extends IntegrationTestBase {

  @Test
  void onlyTheConfiguredOverrideOriginIsGrantedAccess() {
    HttpHeaders overrideOriginHeaders = new HttpHeaders();
    overrideOriginHeaders.add(HttpHeaders.ORIGIN, "http://other.example.com");
    overrideOriginHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
    ResponseEntity<Void> overrideOriginResponse = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.OPTIONS, new HttpEntity<>(null, overrideOriginHeaders), Void.class);
    assertThat(overrideOriginResponse.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(overrideOriginResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isEqualTo("http://other.example.com");

    HttpHeaders previouslyAllowedOriginHeaders = new HttpHeaders();
    previouslyAllowedOriginHeaders.add(HttpHeaders.ORIGIN, "http://allowed.example.com");
    previouslyAllowedOriginHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
    ResponseEntity<Void> previouslyAllowedResponse = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.OPTIONS,
        new HttpEntity<>(null, previouslyAllowedOriginHeaders), Void.class);
    assertThat(previouslyAllowedResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isNull();
  }
}
