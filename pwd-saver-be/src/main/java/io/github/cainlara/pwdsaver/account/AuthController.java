package io.github.cainlara.pwdsaver.account;

import io.github.cainlara.pwdsaver.account.dto.LoginRequest;
import io.github.cainlara.pwdsaver.account.dto.RegisterRequest;
import io.github.cainlara.pwdsaver.account.dto.UserAccountSummary;
import io.github.cainlara.pwdsaver.common.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration, login, and logout (FR-001, FR-002, FR-002a, FR-002b, FR-013).
 * Login/logout are implemented as plain JSON endpoints (rather than Spring
 * Security's form-login filter) so the session-establishment logic can be
 * combined with {@link LoginAttemptService} lockout bookkeeping.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final LoginAttemptService loginAttemptService;
  private final SecurityContextRepository securityContextRepository;

  public AuthController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager, LoginAttemptService loginAttemptService,
      SecurityContextRepository securityContextRepository) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.loginAttemptService = loginAttemptService;
    this.securityContextRepository = securityContextRepository;
  }

  @PostMapping("/register")
  public ResponseEntity<UserAccountSummary> register(@Valid @RequestBody RegisterRequest request) {
    userAccountRepository.findByUsernameOrEmail(request.usernameOrEmail()).ifPresent(existing -> {
      throw new ConflictException("An account with that username/email already exists");
    });

    UserAccount account = new UserAccount(request.usernameOrEmail(),
        passwordEncoder.encode(request.password()));
    userAccountRepository.save(account);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UserAccountSummary(account.getId(), account.getUsernameOrEmail()));
  }

  @PostMapping("/login")
  public ResponseEntity<UserAccountSummary> login(@Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));

      loginAttemptService.recordSuccess(request.usernameOrEmail());

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, httpRequest, httpResponse);

      UserAccount account = userAccountRepository.findByUsernameOrEmail(request.usernameOrEmail())
          .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
      return ResponseEntity.ok(new UserAccountSummary(account.getId(), account.getUsernameOrEmail()));
    } catch (AuthenticationException ex) {
      loginAttemptService.recordFailure(request.usernameOrEmail());
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    request.getSession().invalidate();
    SecurityContextHolder.clearContext();
    return ResponseEntity.noContent().build();
  }
}
