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

/** Verifies FR-009/FR-010: update rejected for non-owned credentials and invalid payloads. */
class UpdateCredentialValidationIT extends IntegrationTestBase {

  private HttpHeaders loginAndGetSessionHeaders(String usernameOrEmail, String password) {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest(usernameOrEmail, password), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest(usernameOrEmail, password), UserAccountSummary.class);
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  void updateRejectedForCredentialNotOwnedByCaller() {
    HttpHeaders ownerHeaders = loginAndGetSessionHeaders("owner@example.com", "Password1!");
    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), ownerHeaders),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    HttpHeaders otherHeaders = loginAndGetSessionHeaders("other@example.com", "Password1!");
    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId), HttpMethod.PUT,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "hacked", null, null), otherHeaders),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void updateRejectedWhenRequiredFieldMissing() {
    HttpHeaders headers = loginAndGetSessionHeaders("update-invalid@example.com", "Password1!");
    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), headers),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId), HttpMethod.PUT,
        new HttpEntity<>(new CredentialVersionInput("", "pass2", null, null), headers),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
