package tn.atb.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tn.atb.backend.entity.enums.Role;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    /**
     * Optional on update: leave null/blank to keep the current password.
     * When provided, must contain at least 8 characters.
     */
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
