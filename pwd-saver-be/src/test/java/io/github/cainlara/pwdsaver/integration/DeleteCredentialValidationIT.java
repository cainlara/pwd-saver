package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import io.github.cainlara.pwdsaver.credential.dto.CredentialEntryResponse;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Verifies FR-009: delete rejected for a credential not owned by the caller, or nonexistent. */
class DeleteCredentialValidationIT extends IntegrationTestBase {

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
  void deleteRejectedForCredentialNotOwnedByCaller() {
    HttpHeaders ownerHeaders = loginAndGetSessionHeaders("delete-owner@example.com", "Password1!");
    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), ownerHeaders),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    HttpHeaders otherHeaders = loginAndGetSessionHeaders("delete-other@example.com", "Password1!");
    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId), HttpMethod.DELETE,
        new HttpEntity<>(null, otherHeaders), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<CredentialEntryResponse[]> ownerList = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, ownerHeaders),
        CredentialEntryResponse[].class);
    assertThat(ownerList.getBody()).hasSize(1);
  }

  @Test
  void deleteOfNonexistentCredentialReturnsNotFound() {
    HttpHeaders headers = loginAndGetSessionHeaders("delete-missing@example.com", "Password1!");

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials/" + UUID.randomUUID()), HttpMethod.DELETE,
        new HttpEntity<>(null, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
