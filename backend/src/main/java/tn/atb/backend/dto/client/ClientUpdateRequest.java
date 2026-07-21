package tn.atb.backend.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import tn.atb.backend.entity.enums.EducationLevel;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;
import tn.atb.backend.entity.enums.PropertyArea;

import java.time.LocalDate;

@Getter
@Setter
public class ClientUpdateRequest {

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

    @NotNull(message = "Number of dependents is required")
    @PositiveOrZero(message = "Number of dependents must be positive")
    private Integer dependents;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Property area is required")
    private PropertyArea propertyArea;

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
