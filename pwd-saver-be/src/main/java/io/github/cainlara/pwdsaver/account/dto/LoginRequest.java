package io.github.cainlara.pwdsaver.account.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String usernameOrEmail,
    @NotBlank String password) {
}
