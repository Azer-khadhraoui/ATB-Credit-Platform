package tn.atb.backend.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;

import java.time.LocalDate;

@Getter
@Setter
public class ClientCreateRequest {

    @NotBlank(message = "CIN is required")
    private String cin;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String profession;

    private String employer;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Monthly income is required")
    @PositiveOrZero(message = "Monthly income must be positive")
    private Double monthlyIncome;

    @PositiveOrZero(message = "Monthly expenses must be positive")
    private Double monthlyExpenses;
}
