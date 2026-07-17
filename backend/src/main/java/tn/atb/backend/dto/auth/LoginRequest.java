package tn.atb.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Matricule is required")
    @Pattern(regexp = "\\d{6}", message = "Matricule must contain exactly 6 digits")
    private String matricule;

    @NotBlank(message = "Password is required")
    private String password;
}
