package io.github.cainlara.pwdsaver.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

  @Mock
  private UserAccountRepository userAccountRepository;

  private LoginAttemptService loginAttemptService;

  @BeforeEach
  void setUp() {
    loginAttemptService = new LoginAttemptService(userAccountRepository, 3);
  }

  @Test
  void recordFailureIncrementsCounterAndDoesNotLockBeforeThreshold() {
    UserAccount account = new UserAccount("user@example.com", "hash");
    when(userAccountRepository.findByUsernameOrEmail("user@example.com"))
        .thenReturn(Optional.of(account));

    loginAttemptService.recordFailure("user@example.com");
    loginAttemptService.recordFailure("user@example.com");

    assertThat(account.getFailedLoginAttempts()).isEqualTo(2);
    assertThat(account.isLocked()).isFalse();
  }

  @Test
  void recordFailureLocksAccountExactlyAtThreshold() {
    UserAccount account = new UserAccount("user@example.com", "hash");
    when(userAccountRepository.findByUsernameOrEmail("user@example.com"))
        .thenReturn(Optional.of(account));

    loginAttemptService.recordFailure("user@example.com");
    loginAttemptService.recordFailure("user@example.com");
    loginAttemptService.recordFailure("user@example.com");

    assertThat(account.getFailedLoginAttempts()).isEqualTo(3);
    assertThat(account.isLocked()).isTrue();
    verify(userAccountRepository, org.mockito.Mockito.times(3)).save(any());
  }

  @Test
  void recordSuccessResetsFailedAttemptCounter() {
    UserAccount account = new UserAccount("user@example.com", "hash");
    account.setFailedLoginAttempts(2);
    when(userAccountRepository.findByUsernameOrEmail("user@example.com"))
        .thenReturn(Optional.of(account));

    loginAttemptService.recordSuccess("user@example.com");

    assertThat(account.getFailedLoginAttempts()).isZero();
  }
}
