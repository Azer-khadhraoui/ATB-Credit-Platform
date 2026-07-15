package tn.atb.backend.mapper;

import org.springframework.stereotype.Component;
import tn.atb.backend.dto.user.UserResponse;
import tn.atb.backend.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .matricule(user.getMatricule())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
