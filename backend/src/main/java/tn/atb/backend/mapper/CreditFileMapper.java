package tn.atb.backend.mapper;

import org.springframework.stereotype.Component;
import tn.atb.backend.dto.creditfile.CreditFileResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;

@Component
public class CreditFileMapper {

    public CreditFileResponse toResponse(CreditFile creditFile, Client client) {
        return CreditFileResponse.builder()
                .id(creditFile.getId())
                .clientId(creditFile.getClientId())
                .clientFullName(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .clientCin(client != null ? client.getCin() : null)
                .createdBy(creditFile.getCreatedBy())
                .creditType(creditFile.getCreditType())
                .loanAmount(creditFile.getLoanAmount())
                .coapplicantIncome(creditFile.getCoapplicantIncome())
                .loanDurationMonths(creditFile.getLoanDurationMonths())
                .loanPurpose(creditFile.getLoanPurpose())
                .interestRate(creditFile.getInterestRate())
                .downPayment(creditFile.getDownPayment())
                .existingCredits(creditFile.getExistingCredits())
                .monthlyInstallment(creditFile.getMonthlyInstallment())
                .creditHistory(creditFile.getCreditHistory())
                .guaranteeType(creditFile.getGuaranteeType())
                .status(creditFile.getStatus())
                .riskScore(creditFile.getRiskScore())
                .riskLevel(creditFile.getRiskLevel())
                .aiDecision(creditFile.getAiDecision())
                .agentDecision(creditFile.getAgentDecision())
                .comments(creditFile.getComments())
                .decisionFactors(creditFile.getDecisionFactors())
                .createdAt(creditFile.getCreatedAt())
                .updatedAt(creditFile.getUpdatedAt())
                .build();
    }
}
