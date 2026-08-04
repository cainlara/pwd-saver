package io.github.cainlara.pwdsaver.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Verifies FR-001/FR-002/FR-003/FR-006/FR-007/FR-009: the self-hosted Scalar
 * documentation UI and the OpenAPI document it renders are both reachable
 * without authentication, cover every existing controller, and never load
 * assets from an external CDN.
 */
class ApiDocumentationIT extends IntegrationTestBase {

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  @Test
  void docsUiIsReachableWithoutAuthentication() {
    ResponseEntity<String> response = restTemplate.getForEntity(url("/scalar"), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void generatedOpenApiDocumentIsReachableWithoutAuthentication() {
    ResponseEntity<String> response = restTemplate.getForEntity(url("/v3/api-docs"), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void generatedOpenApiDocumentListsEveryExistingControllerPath() {
    ResponseEntity<String> response = restTemplate.getForEntity(url("/v3/api-docs"), String.class);
    String body = response.getBody();

    assertThat(body).contains("/api/v1/auth/register");
    assertThat(body).contains("/api/v1/auth/login");
    assertThat(body).contains("/api/v1/auth/logout");
    assertThat(body).contains("/api/v1/credentials");
    assertThat(body).contains("/api/v1/credentials/{credentialId}");
    assertThat(body).contains("/api/v1/credentials/{credentialId}/versions");
    assertThat(body).contains("/api/v1/credentials/{credentialId}/versions/{versionId}");
  }

  @Test
  void docsUiLoadsNoAssetsFromAnExternalCdn() {
    ResponseEntity<String> response = restTemplate.getForEntity(url("/scalar"), String.class);
    String body = response.getBody();

    assertThat(body).doesNotContain("cdn.jsdelivr.net");
    assertThat(body).doesNotContain("unpkg.com");
    assertThat(body).doesNotContain("cdn.scalar.com");
    assertThat(body).doesNotContain("registry.scalar.com");
  }
}
