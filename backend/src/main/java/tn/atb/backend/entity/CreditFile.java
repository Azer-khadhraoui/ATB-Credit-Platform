package tn.atb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tn.atb.backend.entity.enums.AIDecision;
import tn.atb.backend.entity.enums.CreditStatus;
import tn.atb.backend.entity.enums.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "credit_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditFile {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private String createdBy;

    private String creditType;

    private Double loanAmount;

    /** Monthly income of a co-borrower, when the application has one — a risk-model feature. */
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

    /** Why the AI reached its decision — kept so the agent can review it later. */
    private List<DecisionFactor> decisionFactors;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
