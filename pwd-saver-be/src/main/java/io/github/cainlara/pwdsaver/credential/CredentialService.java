package io.github.cainlara.pwdsaver.credential;

import io.github.cainlara.pwdsaver.credential.dto.CredentialEntryResponse;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionInput;
import io.github.cainlara.pwdsaver.credential.dto.CredentialVersionResponse;
import io.github.cainlara.pwdsaver.common.NotFoundException;
import io.github.cainlara.pwdsaver.common.ValidationException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership, validation, versioning, and duplicate-detection rules for
 * Credential Entries (FR-004 through FR-011).
 */
@Service
public class CredentialService {

  private final CredentialEntryRepository credentialEntryRepository;
  private final CredentialVersionRepository credentialVersionRepository;
  private final CredentialEncryptionService credentialEncryptionService;
  private final int descriptionMaxLength;

  public CredentialService(CredentialEntryRepository credentialEntryRepository,
      CredentialVersionRepository credentialVersionRepository,
      CredentialEncryptionService credentialEncryptionService,
      @Value("${password.description.max-length:1000}") int descriptionMaxLength) {
    this.credentialEntryRepository = credentialEntryRepository;
    this.credentialVersionRepository = credentialVersionRepository;
    this.credentialEncryptionService = credentialEncryptionService;
    this.descriptionMaxLength = descriptionMaxLength;
  }

  @Transactional
  public CredentialEntryResponse create(UUID userAccountId, CredentialVersionInput input) {
    validate(input);

    boolean duplicate = findActiveEntries(userAccountId).stream()
        .anyMatch(entry -> currentVersionMatches(entry, input.username(), input.password()));

    CredentialEntry entry = new CredentialEntry(userAccountId);
    credentialEntryRepository.save(entry);

    CredentialVersion version = saveNewVersion(entry.getId(), input);

    List<String> warnings = duplicate
        ? List.of("A credential with this username and password already exists for this service")
        : List.of();

    return toResponse(entry, version, warnings);
  }

  private void validate(CredentialVersionInput input) {
    if (input.description() != null && input.description().length() > descriptionMaxLength) {
      throw new ValidationException(
          "description must not exceed " + descriptionMaxLength + " characters");
    }
  }

  private boolean currentVersionMatches(CredentialEntry entry, String username, String password) {
    return credentialVersionRepository.findFirstByCredentialEntryIdOrderByCreatedAtDesc(entry.getId())
        .map(version -> version.getUsername().equals(username)
            && credentialEncryptionService.decrypt(version.getEncryptedPassword()).equals(password))
        .orElse(false);
  }

  private List<CredentialEntry> findActiveEntries(UUID userAccountId) {
    return credentialEntryRepository.findByUserAccountIdAndStatus(userAccountId, CredentialEntry.Status.active);
  }

  @Transactional(readOnly = true)
  public List<CredentialEntryResponse> listActiveForUser(UUID userAccountId) {
    return findActiveEntries(userAccountId).stream()
        .map(entry -> {
          CredentialVersion current = credentialVersionRepository
              .findFirstByCredentialEntryIdOrderByCreatedAtDesc(entry.getId())
              .orElseThrow(() -> new IllegalStateException(
                  "Credential entry has no versions: " + entry.getId()));
          return toResponse(entry, current, List.of());
        })
        .toList();
  }

  @Transactional
  public CredentialEntryResponse update(UUID userAccountId, UUID credentialId, CredentialVersionInput input) {
    validate(input);

    CredentialEntry entry = credentialEntryRepository.findByIdAndUserAccountId(credentialId, userAccountId)
        .orElseThrow(() -> new NotFoundException("Credential entry not found"));

    if (entry.isDeleted()) {
      throw new NotFoundException("Credential entry has been deleted");
    }

    CredentialVersion version = saveNewVersion(entry.getId(), input);
    return toResponse(entry, version, List.of());
  }

  @Transactional(readOnly = true)
  public List<CredentialVersionResponse> getVersionHistory(UUID userAccountId, UUID credentialId) {
    CredentialEntry entry = credentialEntryRepository.findByIdAndUserAccountId(credentialId, userAccountId)
        .orElseThrow(() -> new NotFoundException("Credential entry not found"));

    return credentialVersionRepository.findByCredentialEntryIdOrderByCreatedAtAsc(entry.getId()).stream()
        .map(this::toVersionResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public CredentialVersionResponse getVersion(UUID userAccountId, UUID credentialId, UUID versionId) {
    credentialEntryRepository.findByIdAndUserAccountId(credentialId, userAccountId)
        .orElseThrow(() -> new NotFoundException("Credential entry not found"));

    CredentialVersion version = credentialVersionRepository.findByIdAndCredentialEntryId(versionId, credentialId)
        .orElseThrow(() -> new NotFoundException("Credential version not found"));

    return toVersionResponse(version);
  }

  @Transactional
  public void delete(UUID userAccountId, UUID credentialId) {
    CredentialEntry entry = credentialEntryRepository.findByIdAndUserAccountId(credentialId, userAccountId)
        .orElseThrow(() -> new NotFoundException("Credential entry not found"));

    entry.setStatus(CredentialEntry.Status.deleted);
    credentialEntryRepository.save(entry);
  }

  private CredentialVersionResponse toVersionResponse(CredentialVersion version) {
    return new CredentialVersionResponse(
        version.getId(),
        version.getUsername(),
        credentialEncryptionService.decrypt(version.getEncryptedPassword()),
        version.getUrl(),
        version.getDescription(),
        version.getCreatedAt());
  }

  private CredentialVersion saveNewVersion(UUID credentialEntryId, CredentialVersionInput input) {
    CredentialVersion version = CredentialVersion.builder()
        .credentialEntryId(credentialEntryId)
        .username(input.username())
        .encryptedPassword(credentialEncryptionService.encrypt(input.password()))
        .url(input.url())
        .description(input.description())
        .build();
    return credentialVersionRepository.save(version);
  }

  private CredentialEntryResponse toResponse(CredentialEntry entry, CredentialVersion version,
      List<String> warnings) {
    return new CredentialEntryResponse(
        entry.getId(),
        entry.getStatus().name(),
        version.getUsername(),
        credentialEncryptionService.decrypt(version.getEncryptedPassword()),
        version.getUrl(),
        version.getDescription(),
        version.getId(),
        entry.getCreatedAt(),
        version.getCreatedAt(),
        warnings);
  }
}
