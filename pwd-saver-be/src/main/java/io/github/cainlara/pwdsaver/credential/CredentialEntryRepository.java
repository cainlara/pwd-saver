package io.github.cainlara.pwdsaver.credential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialEntryRepository extends JpaRepository<CredentialEntry, UUID> {

  List<CredentialEntry> findByUserAccountIdAndStatus(UUID userAccountId, CredentialEntry.Status status);

  Optional<CredentialEntry> findByIdAndUserAccountId(UUID id, UUID userAccountId);
}
