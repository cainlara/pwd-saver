package io.github.cainlara.pwdsaver.credential.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CredentialEntryResponse(
    UUID id,
    String status,
    String username,
    String password,
    String url,
    String description,
    UUID currentVersionId,
    Instant createdAt,
    Instant updatedAt,
    List<String> warnings) {
}
