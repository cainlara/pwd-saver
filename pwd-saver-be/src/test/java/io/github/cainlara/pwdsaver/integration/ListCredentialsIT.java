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

/** Verifies FR-006: listing returns the caller's active credentials (or an empty list). */
class ListCredentialsIT extends IntegrationTestBase {

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
  void listReturnsAllActiveCredentialsWithFullDetails() {
    HttpHeaders headers = loginAndGetSessionHeaders("list-two@example.com", "Password1!");

    restTemplate.exchange(baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("user1", "pass1", "https://a.example", "A"), headers),
        CredentialEntryResponse.class);
    restTemplate.exchange(baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("user2", "pass2", "https://b.example", "B"), headers),
        CredentialEntryResponse.class);

    ResponseEntity<CredentialEntryResponse[]> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, headers),
        CredentialEntryResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
    assertThat(response.getBody())
        .extracting(CredentialEntryResponse::username)
        .containsExactlyInAnyOrder("user1", "user2");
  }

  @Test
  void listReturnsEmptyArrayWhenNoCredentialsSaved() {
    HttpHeaders headers = loginAndGetSessionHeaders("list-empty@example.com", "Password1!");

    ResponseEntity<CredentialEntryResponse[]> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, headers),
        CredentialEntryResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }
}
