package io.github.cainlara.pwdsaver.account.dto;

import java.util.UUID;

public record UserAccountSummary(UUID id, String usernameOrEmail) {
}
