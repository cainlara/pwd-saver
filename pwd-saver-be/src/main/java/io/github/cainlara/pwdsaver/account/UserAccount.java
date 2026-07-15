package io.github.cainlara.pwdsaver.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {

  @Id
  private UUID id;

  @Column(name = "username_or_email", nullable = false, unique = true)
  private String usernameOrEmail;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "failed_login_attempts", nullable = false)
  private int failedLoginAttempts;

  @Column(nullable = false)
  private boolean locked;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UserAccount(String usernameOrEmail, String passwordHash) {
    this.id = UUID.randomUUID();
    this.usernameOrEmail = usernameOrEmail;
    this.passwordHash = passwordHash;
    this.enabled = true;
    this.locked = false;
    this.failedLoginAttempts = 0;
    this.createdAt = Instant.now();
  }
}
