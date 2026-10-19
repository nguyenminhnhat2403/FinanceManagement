package org.example.financemanagement.auth;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    @NotNull
    private UUID challengeId;

    @NotBlank @Pattern(regexp = "[0-9]{6}")
    private String code;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

