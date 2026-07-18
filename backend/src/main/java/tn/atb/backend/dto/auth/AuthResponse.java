package tn.atb.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tn.atb.backend.entity.enums.Role;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String id;
    private String matricule;
    private String fullName;
    private Role role;
    private String photoUrl;
}
