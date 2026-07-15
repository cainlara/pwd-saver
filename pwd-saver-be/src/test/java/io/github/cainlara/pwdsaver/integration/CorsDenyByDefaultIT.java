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
 * Proves the deny-by-default behavior (FR-006): with {@code cors.allowed-origins}
 * blank, no origin — not even one that would otherwise be allowed elsewhere — is
 * granted cross-origin access.
 */
@TestPropertySource(properties = "cors.allowed-origins=")
class CorsDenyByDefaultIT extends IntegrationTestBase {

  @Test
  void noOriginIsGrantedAccessWhenAllowedOriginsIsBlank() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ORIGIN, "http://allowed.example.com");
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.OPTIONS, new HttpEntity<>(null, headers), Void.class);

    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
  }
}
