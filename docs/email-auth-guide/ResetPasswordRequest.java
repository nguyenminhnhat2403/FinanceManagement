package org.example.financemanagement.auth.email;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotNull UUID challengeId,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
        @NotBlank @Size(min = 6) String newPassword
) {}

