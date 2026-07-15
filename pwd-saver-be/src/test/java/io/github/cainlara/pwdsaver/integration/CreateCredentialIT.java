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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Verifies FR-004 and FR-005: creating a credential entry, with and without optional fields. */
class CreateCredentialIT extends IntegrationTestBase {

  private HttpHeaders loginAndGetSessionHeaders(String usernameOrEmail, String password) {
    restTemplate.postForEntity(baseUrl("/auth/register"),
        new RegisterRequest(usernameOrEmail, password), Void.class);
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest(usernameOrEmail, password), UserAccountSummary.class);
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    return headers;
  }

  @Test
  void createsCredentialWithOnlyRequiredFields() {
    HttpHeaders headers = loginAndGetSessionHeaders("create-min@example.com", "Password1!");
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    CredentialVersionInput input = new CredentialVersionInput("svc-user", "svc-pass", null, null);
    ResponseEntity<CredentialEntryResponse> response = restTemplate.exchange(
        baseUrl("/credentials"), org.springframework.http.HttpMethod.POST,
        new HttpEntity<>(input, headers), CredentialEntryResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().username()).isEqualTo("svc-user");
    assertThat(response.getBody().password()).isEqualTo("svc-pass");
    assertThat(response.getBody().warnings()).isEmpty();
  }

  @Test
  void createsCredentialWithUrlAndDescription() {
    HttpHeaders headers = loginAndGetSessionHeaders("create-full@example.com", "Password1!");
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    CredentialVersionInput input = new CredentialVersionInput(
        "svc-user", "svc-pass", "https://example.com", "Personal email");
    ResponseEntity<CredentialEntryResponse> response = restTemplate.exchange(
        baseUrl("/credentials"), org.springframework.http.HttpMethod.POST,
        new HttpEntity<>(input, headers), CredentialEntryResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().url()).isEqualTo("https://example.com");
    assertThat(response.getBody().description()).isEqualTo("Personal email");
  }
}
