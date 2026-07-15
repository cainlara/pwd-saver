package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.common.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Verifies FR-002a: login is rejected identically for an unknown account and
 * for a wrong password on a known account, never revealing which reason applied.
 */
class AuthRejectionIT extends IntegrationTestBase {

  @Test
  void loginRejectedForUnknownAccount() {
    LoginRequest login = new LoginRequest("unknown-user@example.com", "whatever");
    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        baseUrl("/auth/login"), login, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().message()).isEqualTo("Invalid username or password");
  }

  @Test
  void loginRejectedForWrongPasswordWithSameMessageAsUnknownAccount() {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest("bob@example.com", "CorrectPass1!"), Void.class);

    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("bob@example.com", "WrongPass"), ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().message()).isEqualTo("Invalid username or password");
  }
}
