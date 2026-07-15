package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Verifies FR-010: creation is rejected when a required field is missing. */
class CreateCredentialValidationIT extends IntegrationTestBase {

  @Test
  void createRejectedWhenUsernameOrPasswordMissing() {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest("create-invalid@example.com", "Password1!"), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("create-invalid@example.com", "Password1!"),
        UserAccountSummary.class);

    HttpHeaders headers = new HttpHeaders();
    headers.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    headers.setContentType(MediaType.APPLICATION_JSON);

    CredentialVersionInput missingPassword = new CredentialVersionInput("svc-user", "", null, null);
    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST, new HttpEntity<>(missingPassword, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
