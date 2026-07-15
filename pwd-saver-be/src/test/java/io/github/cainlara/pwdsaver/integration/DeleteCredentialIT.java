package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import io.github.cainlara.pwdsaver.credential.dto.CredentialEntryResponse;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Verifies FR-008/FR-008a/FR-008b/FR-011: soft delete hides but retains data and history. */
class DeleteCredentialIT extends IntegrationTestBase {

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
  void deleteHidesEntryFromDefaultListButRetainsDataAndHistory() {
    HttpHeaders headers = loginAndGetSessionHeaders("delete-basic@example.com", "Password1!");

    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), headers),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    restTemplate.exchange(baseUrl("/credentials/" + credentialId), HttpMethod.PUT,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass2", null, null), headers),
        CredentialEntryResponse.class);

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId), HttpMethod.DELETE,
        new HttpEntity<>(null, headers), Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<CredentialEntryResponse[]> list = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, headers),
        CredentialEntryResponse[].class);
    assertThat(list.getBody()).isEmpty();

    ResponseEntity<CredentialVersionResponse[]> history = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId + "/versions"), HttpMethod.GET,
        new HttpEntity<>(null, headers), CredentialVersionResponse[].class);
    assertThat(history.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(history.getBody()).hasSize(2);
  }

  @Test
  void endToEndDeleteThenUpdateIsRejected() {
    HttpHeaders headers = loginAndGetSessionHeaders("delete-then-update@example.com", "Password1!");

    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), headers),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    restTemplate.exchange(baseUrl("/credentials/" + credentialId), HttpMethod.DELETE,
        new HttpEntity<>(null, headers), Void.class);

    ResponseEntity<Void> updateAfterDelete = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId), HttpMethod.PUT,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass2", null, null), headers),
        Void.class);

    assertThat(updateAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
