package tn.atb.backend.mapper;

import org.springframework.stereotype.Component;
import tn.atb.backend.dto.client.ClientResponse;
import tn.atb.backend.entity.Client;

@Component
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .cin(client.getCin())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .birthDate(client.getBirthDate())
                .gender(client.getGender())
                .maritalStatus(client.getMaritalStatus())
                .dependents(client.getDependents())
                .educationLevel(client.getEducationLevel())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                .city(client.getCity())
                .propertyArea(client.getPropertyArea())
                .profession(client.getProfession())
                .employer(client.getEmployer())
                .employmentType(client.getEmploymentType())
                .monthlyIncome(client.getMonthlyIncome())
                .monthlyExpenses(client.getMonthlyExpenses())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
