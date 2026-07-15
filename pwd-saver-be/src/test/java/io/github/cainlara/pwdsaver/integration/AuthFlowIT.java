package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthFlowIT extends IntegrationTestBase {

  @Test
  void registerThenLoginThenLogoutSucceeds() {
    RegisterRequest register = new RegisterRequest("alice@example.com", "S3curePass!");
    ResponseEntity<UserAccountSummary> registerResponse = restTemplate.postForEntity(
        baseUrl("/auth/register"), register, UserAccountSummary.class);
    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    LoginRequest login = new LoginRequest("alice@example.com", "S3curePass!");
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.postForEntity(
        baseUrl("/auth/login"), login, UserAccountSummary.class);
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody().usernameOrEmail()).isEqualTo("alice@example.com");

    HttpHeaders sessionHeaders = new HttpHeaders();
    sessionHeaders.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

    ResponseEntity<Void> logoutResponse = restTemplate.exchange(
        baseUrl("/auth/logout"), HttpMethod.POST, new HttpEntity<>(null, sessionHeaders), Void.class);
    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }
}
