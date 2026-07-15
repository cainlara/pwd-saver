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

/** Verifies FR-009 / SC-003: a user never sees another user's credential entries. */
class CredentialIsolationIT extends IntegrationTestBase {

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
  void oneUsersListNeverIncludesAnotherUsersCredentials() {
    HttpHeaders userAHeaders = loginAndGetSessionHeaders("isolation-a@example.com", "Password1!");
    restTemplate.exchange(baseUrl("/credentials"), HttpMethod.POST,
        new HttpEntity<>(new CredentialVersionInput("a-user", "a-pass", null, null), userAHeaders),
        CredentialEntryResponse.class);

    HttpHeaders userBHeaders = loginAndGetSessionHeaders("isolation-b@example.com", "Password1!");

    ResponseEntity<CredentialEntryResponse[]> userBList = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, userBHeaders),
        CredentialEntryResponse[].class);

    assertThat(userBList.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(userBList.getBody()).isEmpty();
  }
}
