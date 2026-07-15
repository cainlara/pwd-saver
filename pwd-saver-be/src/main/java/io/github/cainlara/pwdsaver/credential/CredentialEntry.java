package io.github.cainlara.pwdsaver.credential;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credential_entry")
@Getter
@Setter
@NoArgsConstructor
public class CredentialEntry {

  public enum Status {
    active,
    deleted
  }

  @Id
  private UUID id;

  @Column(name = "user_account_id", nullable = false, updatable = false)
  private UUID userAccountId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public CredentialEntry(UUID userAccountId) {
    this.id = UUID.randomUUID();
    this.userAccountId = userAccountId;
    this.status = Status.active;
    this.createdAt = Instant.now();
  }

  public boolean isDeleted() {
    return status == Status.deleted;
  }
}
