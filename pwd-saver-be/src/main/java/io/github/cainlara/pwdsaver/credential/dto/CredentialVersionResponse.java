package io.github.cainlara.pwdsaver.credential.dto;

import java.time.Instant;
import java.util.UUID;

public record CredentialVersionResponse(
    UUID versionId,
    String username,
    String password,
    String url,
    String description,
    Instant createdAt) {
}
