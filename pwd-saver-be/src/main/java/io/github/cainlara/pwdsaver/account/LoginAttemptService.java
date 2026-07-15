package io.github.cainlara.pwdsaver.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tracks consecutive failed login attempts per account and locks the account
 * once the configured threshold is reached (FR-002b). Unlocking a locked
 * account is intentionally not implemented (spec Assumptions: future feature).
 */
@Service
public class LoginAttemptService {

  private final UserAccountRepository userAccountRepository;
  private final int maxFailedLoginAttempts;

  public LoginAttemptService(UserAccountRepository userAccountRepository,
      @Value("${password.policy.max-failed-login-attempts:3}") int maxFailedLoginAttempts) {
    this.userAccountRepository = userAccountRepository;
    this.maxFailedLoginAttempts = maxFailedLoginAttempts;
  }

  @Transactional
  public void recordFailure(String usernameOrEmail) {
    userAccountRepository.findByUsernameOrEmail(usernameOrEmail).ifPresent(account -> {
      if (account.isLocked()) {
        return;
      }
      account.setFailedLoginAttempts(account.getFailedLoginAttempts() + 1);
      if (account.getFailedLoginAttempts() >= maxFailedLoginAttempts) {
        account.setLocked(true);
      }
      userAccountRepository.save(account);
    });
  }

  @Transactional
  public void recordSuccess(String usernameOrEmail) {
    userAccountRepository.findByUsernameOrEmail(usernameOrEmail).ifPresent(account -> {
      account.setFailedLoginAttempts(0);
      userAccountRepository.save(account);
    });
  }
}
