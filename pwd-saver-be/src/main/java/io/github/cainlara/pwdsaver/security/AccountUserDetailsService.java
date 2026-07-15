package io.github.cainlara.pwdsaver.security;

import io.github.cainlara.pwdsaver.account.UserAccount;
import io.github.cainlara.pwdsaver.account.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapts {@link UserAccount} to Spring Security's {@link UserDetails}, mapping
 * the account's enabled/locked state so the framework rejects disabled or
 * locked accounts before password verification (FR-002a).
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

  private final UserAccountRepository userAccountRepository;

  public AccountUserDetailsService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    UserAccount account = userAccountRepository.findByUsernameOrEmail(usernameOrEmail)
        .orElseThrow(() -> new UsernameNotFoundException("No such account"));

    return User.builder()
        .username(account.getUsernameOrEmail())
        .password(account.getPasswordHash())
        .disabled(!account.isEnabled())
        .accountLocked(account.isLocked())
        .build();
  }
}
