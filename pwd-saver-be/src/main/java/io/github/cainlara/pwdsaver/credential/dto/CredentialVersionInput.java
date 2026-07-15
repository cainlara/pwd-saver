package io.github.cainlara.pwdsaver.credential.dto;

import jakarta.validation.constraints.NotBlank;

public record CredentialVersionInput(
    @NotBlank String username,
    @NotBlank String password,
    String url,
    String description) {
}
