package io.github.cainlara.pwdsaver.credential;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cainlara.pwdsaver.IntegrationTestBase;
import io.github.cainlara.pwdsaver.account.UserAccount;
import io.github.cainlara.pwdsaver.account.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Focused repository-slice test: confirms the "current version" query
 * (used throughout {@link CredentialService}) always returns the version
 * with the latest {@code created_at}, regardless of insertion order.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CredentialVersionSelectionTest {

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", IntegrationTestBase.POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", IntegrationTestBase.POSTGRES::getUsername);
    registry.add("spring.datasource.password", IntegrationTestBase.POSTGRES::getPassword);
  }

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private CredentialEntryRepository credentialEntryRepository;

  @Autowired
  private CredentialVersionRepository credentialVersionRepository;

  @Test
  void findFirstOrderByCreatedAtDescReturnsMostRecentVersionRegardlessOfInsertionOrder() {
    UserAccount account = new UserAccount("selection-test@example.com", "hash");
    userAccountRepository.save(account);

    CredentialEntry entry = new CredentialEntry(account.getId());
    credentialEntryRepository.save(entry);

    CredentialVersion older = CredentialVersion.builder()
        .credentialEntryId(entry.getId())
        .username("user")
        .encryptedPassword("cipher-older")
        .build();
    credentialVersionRepository.save(older);
    credentialVersionRepository.flush();

    // Ensure a strictly later created_at than `older`, independent of clock resolution.
    try {
      Thread.sleep(5);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    CredentialVersion newer = CredentialVersion.builder()
        .credentialEntryId(entry.getId())
        .username("user")
        .encryptedPassword("cipher-newer")
        .build();
    credentialVersionRepository.save(newer);

    var current = credentialVersionRepository
        .findFirstByCredentialEntryIdOrderByCreatedAtDesc(entry.getId())
        .orElseThrow();

    assertThat(current.getEncryptedPassword()).isEqualTo("cipher-newer");
  }
}
