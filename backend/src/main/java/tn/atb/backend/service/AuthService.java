package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import tn.atb.backend.dto.auth.AuthResponse;
import tn.atb.backend.dto.auth.LoginRequest;
import tn.atb.backend.entity.User;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.repository.UserRepository;
import tn.atb.backend.security.JwtService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (handled globally -> 401) when the matricule/password pair is wrong.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getMatricule(), request.getPassword()));

        User user = userRepository.findByMatricule(request.getMatricule())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with matricule: " + request.getMatricule()));

        String fullName = user.getFirstName() + " " + user.getLastName();
        String token = jwtService.generateToken(user.getMatricule(), Map.of(
                "role", user.getRole().name(),
                "fullName", fullName
        ));

        auditLogService.log(user.getMatricule(), AuditAction.LOGIN, "User", user.getId(),
                fullName + " s'est connecté(e)");

        return AuthResponse.builder()
                .token(token)
                .matricule(user.getMatricule())
                .fullName(fullName)
                .role(user.getRole())
                .photoUrl(user.getPhotoUrl())
                .build();
    }
}
