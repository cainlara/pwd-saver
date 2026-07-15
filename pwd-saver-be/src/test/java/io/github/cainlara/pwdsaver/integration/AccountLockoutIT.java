package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.common.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Verifies FR-002b: the account locks after 3 consecutive failed logins. */
class AccountLockoutIT extends IntegrationTestBase {

  @Test
  void accountLocksAfterThreeFailedAttemptsAndRejectsEvenCorrectPasswordAfterward() {
    String usernameOrEmail = "carol@example.com";
    String correctPassword = "CorrectPass1!";
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest(usernameOrEmail, correctPassword), Void.class);

    for (int attempt = 1; attempt <= 3; attempt++) {
      ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
          baseUrl("/auth/login"), new LoginRequest(usernameOrEmail, "wrong-password"),
          ErrorResponse.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    ResponseEntity<ErrorResponse> lockedResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest(usernameOrEmail, correctPassword),
        ErrorResponse.class);

    assertThat(lockedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
