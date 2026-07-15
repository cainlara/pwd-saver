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

/** Verifies FR-007d: looking up a version that doesn't exist returns a clean error. */
class CredentialVersionLookupIT extends IntegrationTestBase {

  @Test
  void requestingNonexistentVersionReturnsNotFound() {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest("version-lookup@example.com", "Password1!"), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("version-lookup@example.com", "Password1!"),
        UserAccountSummary.class);
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<CredentialEntryResponse> created = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("svc-user", "pass1", null, null), headers),
        CredentialEntryResponse.class);
    var credentialId = created.getBody().id();

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials/" + credentialId + "/versions/" + UUID.randomUUID()),
        HttpMethod.GET, new HttpEntity<>(null, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
