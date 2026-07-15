package io.github.cainlara.pwdsaver.credential;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only snapshot of a {@link CredentialEntry}'s data at a point in time.
 * Instances are never mutated after being persisted (FR-007).
 */
@Entity
@Table(name = "credential_version")
@Getter
@NoArgsConstructor
public class CredentialVersion {

  @Id
  private UUID id;

  @Column(name = "credential_entry_id", nullable = false, updatable = false)
  private UUID credentialEntryId;

  @Column(nullable = false)
  private String username;

  @Column(name = "encrypted_password", nullable = false, columnDefinition = "TEXT")
  private String encryptedPassword;

  @Column
  private String url;

  @Column
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Builder
  public CredentialVersion(UUID credentialEntryId, String username, String encryptedPassword,
      String url, String description) {
    this.id = UUID.randomUUID();
    this.credentialEntryId = credentialEntryId;
    this.username = username;
    this.encryptedPassword = encryptedPassword;
    this.url = url;
    this.description = description;
    this.createdAt = Instant.now();
  }
}
