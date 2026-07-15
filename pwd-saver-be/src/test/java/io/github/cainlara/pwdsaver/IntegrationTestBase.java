package io.github.cainlara.pwdsaver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton-container pattern: a single PostgreSQL container is started once
 * for the whole test JVM and never stopped by JUnit's per-class lifecycle.
 * This is deliberate: with a per-class {@code @Testcontainers}/{@code @Container}
 * (restarted for every test class), Spring's test {@code ApplicationContext}
 * cache still reuses one cached context across "identical" test classes,
 * which then holds a stale JDBC URL pointing at an already-stopped container.
 * Starting exactly one long-lived container sidesteps that mismatch entirely.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

  static {
    POSTGRES.start();
    awaitRealConnectivity();
  }

  /**
   * Testcontainers' own readiness check (container-internal log line) can
   * momentarily precede the mapped port actually being reachable from the host
   * on some Docker networking setups (observed with Rancher Desktop's VM-based
   * port forwarding). Confirm a real JDBC connection succeeds before Spring
   * attempts to use this datasource, to avoid a rare "connection refused" race.
   */
  private static void awaitRealConnectivity() {
    SQLException lastFailure = null;
    for (int attempt = 0; attempt < 20; attempt++) {
      try (Connection connection = DriverManager.getConnection(
          POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
        if (connection.isValid(2)) {
          return;
        }
      } catch (SQLException e) {
        lastFailure = e;
        try {
          Thread.sleep(250);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("Interrupted while waiting for Postgres container", interrupted);
        }
      }
    }
    throw new IllegalStateException("Postgres container never became reachable", lastFailure);
  }

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @LocalServerPort
  protected int port;

  @Autowired
  protected TestRestTemplate restTemplate;

  protected String baseUrl(String path) {
    return "http://localhost:" + port + "/api/v1" + path;
  }
}
