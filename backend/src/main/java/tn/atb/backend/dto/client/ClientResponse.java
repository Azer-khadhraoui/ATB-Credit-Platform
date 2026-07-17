package tn.atb.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ClientResponse {

    private String id;
    private String cin;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String profession;
    private String employer;
    private EmploymentType employmentType;
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
