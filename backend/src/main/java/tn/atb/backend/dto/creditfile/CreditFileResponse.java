package tn.atb.backend.dto.creditfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tn.atb.backend.entity.DecisionFactor;
import tn.atb.backend.entity.enums.AIDecision;
import tn.atb.backend.entity.enums.CreditStatus;
import tn.atb.backend.entity.enums.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CreditFileResponse {

    private String id;
    private String clientId;
    private String clientFullName;
    private String clientCin;
    private String createdBy;
    private String creditType;
    private Double loanAmount;
    private Double coapplicantIncome;
    private Integer loanDurationMonths;
    private String loanPurpose;
    private Double interestRate;
    private Double downPayment;
    private Integer existingCredits;
    private Double monthlyInstallment;
    private String creditHistory;
    private String guaranteeType;
    private CreditStatus status;
    private Double riskScore;
    private RiskLevel riskLevel;
    private AIDecision aiDecision;
    private String agentDecision;
    private String comments;
    private List<DecisionFactor> decisionFactors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
