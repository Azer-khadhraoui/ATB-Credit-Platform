package tn.atb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    private String id;

    @Indexed(unique = true)
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
