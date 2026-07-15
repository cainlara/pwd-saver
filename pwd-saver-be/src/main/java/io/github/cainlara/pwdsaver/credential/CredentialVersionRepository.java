package io.github.cainlara.pwdsaver.credential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialVersionRepository extends JpaRepository<CredentialVersion, UUID> {

  List<CredentialVersion> findByCredentialEntryIdOrderByCreatedAtAsc(UUID credentialEntryId);

  Optional<CredentialVersion> findFirstByCredentialEntryIdOrderByCreatedAtDesc(UUID credentialEntryId);

  Optional<CredentialVersion> findByIdAndCredentialEntryId(UUID id, UUID credentialEntryId);
}
