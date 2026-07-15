package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import io.github.cainlara.pwdsaver.credential.dto.CredentialEntryResponse;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Verifies FR-004a: duplicate username/password for a service is allowed with a warning. */
class DuplicateCredentialIT extends IntegrationTestBase {

  @Test
  void duplicateCredentialIsAcceptedWithWarning() {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest("dup@example.com", "Password1!"), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("dup@example.com", "Password1!"),
        UserAccountSummary.class);

    HttpHeaders headers = new HttpHeaders();
    headers.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    headers.setContentType(MediaType.APPLICATION_JSON);

    CredentialVersionInput input = new CredentialVersionInput("svc-user", "svc-pass", null, null);

    ResponseEntity<CredentialEntryResponse> first = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST, new HttpEntity<>(input, headers),
        CredentialEntryResponse.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(first.getBody().warnings()).isEmpty();

    ResponseEntity<CredentialEntryResponse> second = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST, new HttpEntity<>(input, headers),
        CredentialEntryResponse.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(second.getBody().warnings()).isNotEmpty();
  }
}
