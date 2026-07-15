package io.github.cainlara.pwdsaver.account.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String usernameOrEmail,
    @NotBlank String password) {
}
