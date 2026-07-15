package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Verifies FR-003 and FR-003a/FR-013: vault access requires a live, authenticated session. */
class SessionExpiryIT extends IntegrationTestBase {

  @Test
  void vaultRequestRejectedWithoutAuthentication() {
    ResponseEntity<Void> response = restTemplate.getForEntity(baseUrl("/credentials"), Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void vaultRequestRejectedAfterLogout() {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest("dave@example.com", "CorrectPass1!"), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("dave@example.com", "CorrectPass1!"),
        UserAccountSummary.class);

    HttpHeaders sessionHeaders = new HttpHeaders();
    sessionHeaders.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

    restTemplate.exchange(baseUrl("/auth/logout"), HttpMethod.POST,
        new HttpEntity<>(null, sessionHeaders), Void.class);

    ResponseEntity<Void> afterLogout = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, sessionHeaders), Void.class);
    assertThat(afterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
