package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.atb.backend.dto.user.UserCreateRequest;
import tn.atb.backend.dto.user.UserResponse;
import tn.atb.backend.dto.user.UserUpdateRequest;
import tn.atb.backend.entity.User;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.exception.DuplicateResourceException;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.mapper.UserMapper;
import tn.atb.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new DuplicateResourceException("A user with this matricule already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .matricule(request.getMatricule())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        String fullName = saved.getFirstName() + " " + saved.getLastName();
        auditLogService.log(saved.getMatricule(), AuditAction.CREATE, "User", saved.getId(),
                "Création du compte " + fullName);

        return userMapper.toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(String id) {
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = findUserOrThrow(id);

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        auditLogService.log(AuditAction.UPDATE, "User", saved.getId(),
                "Modification du compte " + saved.getFirstName() + " " + saved.getLastName());

        return userMapper.toResponse(saved);
    }

    public UserResponse updatePhoto(String id, MultipartFile file) {
        User user = findUserOrThrow(id);
        String photoUrl = fileStorageService.storeImage(file, "user-" + id);
        user.setPhotoUrl(photoUrl);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public void deleteUser(String id) {
        User user = findUserOrThrow(id);
        String fullName = user.getFirstName() + " " + user.getLastName();
        userRepository.delete(user);

        auditLogService.log(AuditAction.DELETE, "User", id, "Suppression du compte " + fullName);
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
