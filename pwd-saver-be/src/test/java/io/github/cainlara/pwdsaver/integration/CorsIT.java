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

class CorsIT extends IntegrationTestBase {

  private static final String ALLOWED_ORIGIN = "http://allowed.example.com";
  private static final String DISALLOWED_ORIGIN = "http://evil.example.com";

  private String actuatorUrl(String path) {
    return "http://localhost:" + port + path;
  }

  @Test
  void allowedOriginCanLoginAndMakeAuthenticatedFollowUpCall() {
    RegisterRequest register = new RegisterRequest("cors-allowed@example.com", "S3curePass!");
    restTemplate.postForEntity(baseUrl("/auth/register"), register, UserAccountSummary.class);

    HttpHeaders loginHeaders = new HttpHeaders();
    loginHeaders.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    LoginRequest login = new LoginRequest("cors-allowed@example.com", "S3curePass!");
    ResponseEntity<UserAccountSummary> loginResponse = restTemplate.exchange(
        baseUrl("/auth/login"), HttpMethod.POST, new HttpEntity<>(login, loginHeaders),
        UserAccountSummary.class);
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isEqualTo(ALLOWED_ORIGIN);
    assertThat(loginResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
        .isEqualTo("true");

    HttpHeaders followUpHeaders = new HttpHeaders();
    followUpHeaders.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    followUpHeaders.addAll(HttpHeaders.COOKIE, loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    ResponseEntity<String> credentialsResponse = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.GET, new HttpEntity<>(null, followUpHeaders), String.class);
    assertThat(credentialsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(credentialsResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isEqualTo(ALLOWED_ORIGIN);
    assertThat(credentialsResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
        .isEqualTo("true");
  }

  @Test
  void preflightForAllowedOriginAuthorizesTheRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type");

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.OPTIONS, new HttpEntity<>(null, headers), Void.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isEqualTo(ALLOWED_ORIGIN);
    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
        .contains("POST");
    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
        .contains("Content-Type");
  }

  @Test
  void disallowedOriginActualRequestIsRejectedBeforeBusinessLogicExecutes() {
    RegisterRequest register = new RegisterRequest("cors-disallowed@example.com", "S3curePass!");
    restTemplate.postForEntity(baseUrl("/auth/register"), register, UserAccountSummary.class);

    HttpHeaders disallowedOriginHeaders = new HttpHeaders();
    disallowedOriginHeaders.add(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN);
    LoginRequest wrongPasswordLogin = new LoginRequest("cors-disallowed@example.com", "WrongPassword!");

    // Repeat past the account-lockout threshold (3 failed attempts) to prove these
    // disallowed-origin requests never reach AuthController/LoginAttemptService at all.
    for (int attempt = 0; attempt < 5; attempt++) {
      ResponseEntity<String> rejected = restTemplate.exchange(
          baseUrl("/auth/login"), HttpMethod.POST,
          new HttpEntity<>(wrongPasswordLogin, disallowedOriginHeaders), String.class);
      assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(rejected.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    ResponseEntity<UserAccountSummary> legitimateLogin = restTemplate.postForEntity(
        baseUrl("/auth/login"), new LoginRequest("cors-disallowed@example.com", "S3curePass!"),
        UserAccountSummary.class);
    assertThat(legitimateLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void preflightForDisallowedOriginDoesNotAuthorizeTheRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN);
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl("/credentials"), HttpMethod.OPTIONS, new HttpEntity<>(null, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)).isNull();
  }

  @Test
  void unsupportedMethodOnExistingRouteFailsTheSameWayRegardlessOfCorsPreflight() {
    // /auth/login only maps POST; confirm CORS's global, path-based method allow-list
    // (which does grant DELETE generically at the preflight stage) doesn't change what
    // happens when the actual request reaches routing: it fails exactly as it already
    // would without an Origin header at all — introducing CORS doesn't paper over or
    // worsen an unsupported-method request, it's simply orthogonal to it.
    HttpHeaders preflightHeaders = new HttpHeaders();
    preflightHeaders.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    preflightHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE");
    ResponseEntity<Void> preflight = restTemplate.exchange(
        baseUrl("/auth/login"), HttpMethod.OPTIONS, new HttpEntity<>(null, preflightHeaders), Void.class);
    assertThat(preflight.getStatusCode().is2xxSuccessful()).isTrue();

    ResponseEntity<String> withoutOrigin = restTemplate.exchange(
        baseUrl("/auth/login"), HttpMethod.DELETE, new HttpEntity<>(null, new HttpHeaders()), String.class);

    HttpHeaders actualHeaders = new HttpHeaders();
    actualHeaders.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    ResponseEntity<String> withOrigin = restTemplate.exchange(
        baseUrl("/auth/login"), HttpMethod.DELETE, new HttpEntity<>(null, actualHeaders), String.class);

    assertThat(withOrigin.getStatusCode()).isEqualTo(withoutOrigin.getStatusCode());
  }

  @Test
  void malformedOrLiteralNullOriginNeverMatchesAnyAllowedOrigin() {
    for (String origin : new String[] {"null", "not-a-valid-origin", ""}) {
      HttpHeaders headers = new HttpHeaders();
      if (!origin.isEmpty()) {
        headers.add(HttpHeaders.ORIGIN, origin);
      }
      ResponseEntity<String> response = restTemplate.exchange(
          actuatorUrl("/actuator/health"), HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
      assertThat(response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }
  }

  @Test
  void healthEndpointHonorsTheSameAllowListAsTheRestOfTheApi() {
    HttpHeaders allowedHeaders = new HttpHeaders();
    allowedHeaders.add(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
    ResponseEntity<String> allowedResponse = restTemplate.exchange(
        actuatorUrl("/actuator/health"), HttpMethod.GET, new HttpEntity<>(null, allowedHeaders), String.class);
    assertThat(allowedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(allowedResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        .isEqualTo(ALLOWED_ORIGIN);

    HttpHeaders disallowedHeaders = new HttpHeaders();
    disallowedHeaders.add(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN);
    ResponseEntity<String> disallowedResponse = restTemplate.exchange(
        actuatorUrl("/actuator/health"), HttpMethod.GET, new HttpEntity<>(null, disallowedHeaders), String.class);
    assertThat(disallowedResponse.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
  }
}
