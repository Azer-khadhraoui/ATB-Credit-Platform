package tn.atb.backend.dto.creditfile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tn.atb.backend.entity.enums.CreditStatus;

@Getter
@Setter
public class CreditFileUpdateRequest {

    @NotBlank(message = "Credit type is required")
    private String creditType;

    @NotNull(message = "Loan amount is required")
    @PositiveOrZero(message = "Loan amount must be positive")
    private Double loanAmount;

    @PositiveOrZero(message = "Co-applicant income must be positive")
    private Double coapplicantIncome;

    @NotNull(message = "Loan duration is required")
    @Min(value = 1, message = "Loan duration must be at least 1 month")
    private Integer loanDurationMonths;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;

    @PositiveOrZero(message = "Interest rate must be positive")
    private Double interestRate;

    @PositiveOrZero(message = "Down payment must be positive")
    private Double downPayment;

    @PositiveOrZero(message = "Existing credits must be positive")
    private Integer existingCredits;

    @PositiveOrZero(message = "Monthly installment must be positive")
    private Double monthlyInstallment;

    private String creditHistory;

    private String guaranteeType;

    private CreditStatus status;

    private String agentDecision;

    @Size(max = 500, message = "Comments must not exceed 500 characters")
    private String comments;
}
