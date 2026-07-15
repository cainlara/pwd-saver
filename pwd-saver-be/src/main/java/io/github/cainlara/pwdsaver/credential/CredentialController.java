package io.github.cainlara.pwdsaver.credential;

import io.github.cainlara.pwdsaver.account.UserAccount;
import io.github.cainlara.pwdsaver.account.UserAccountRepository;
import io.github.cainlara.pwdsaver.credential.dto.CredentialEntryResponse;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CRUD and version-history operations on the caller's own credential vault. */
@RestController
@RequestMapping("/api/v1/credentials")
public class CredentialController {

  private final CredentialService credentialService;
  private final UserAccountRepository userAccountRepository;

  public CredentialController(CredentialService credentialService,
      UserAccountRepository userAccountRepository) {
    this.credentialService = credentialService;
    this.userAccountRepository = userAccountRepository;
  }

  @GetMapping
  public ResponseEntity<List<CredentialEntryResponse>> list(Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    return ResponseEntity.ok(credentialService.listActiveForUser(userAccountId));
  }

  @PostMapping
  public ResponseEntity<CredentialEntryResponse> create(
      @Valid @RequestBody CredentialVersionInput input, Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    CredentialEntryResponse response = credentialService.create(userAccountId, input);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{credentialId}")
  public ResponseEntity<CredentialEntryResponse> update(@PathVariable UUID credentialId,
      @Valid @RequestBody CredentialVersionInput input, Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    return ResponseEntity.ok(credentialService.update(userAccountId, credentialId, input));
  }

  @GetMapping("/{credentialId}/versions")
  public ResponseEntity<List<CredentialVersionResponse>> versionHistory(
      @PathVariable UUID credentialId, Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    return ResponseEntity.ok(credentialService.getVersionHistory(userAccountId, credentialId));
  }

  @GetMapping("/{credentialId}/versions/{versionId}")
  public ResponseEntity<CredentialVersionResponse> version(@PathVariable UUID credentialId,
      @PathVariable UUID versionId, Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    return ResponseEntity.ok(credentialService.getVersion(userAccountId, credentialId, versionId));
  }

  @DeleteMapping("/{credentialId}")
  public ResponseEntity<Void> delete(@PathVariable UUID credentialId, Authentication authentication) {
    UUID userAccountId = currentUserAccountId(authentication);
    credentialService.delete(userAccountId, credentialId);
    return ResponseEntity.noContent().build();
  }

  private UUID currentUserAccountId(Authentication authentication) {
    UserAccount account = userAccountRepository.findByUsernameOrEmail(authentication.getName())
        .orElseThrow(() -> new IllegalStateException("Authenticated principal has no account"));
    return account.getId();
  }
}
