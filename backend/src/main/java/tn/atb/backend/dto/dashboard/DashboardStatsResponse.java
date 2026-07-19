package tn.atb.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalClients;
    private long totalCreditFiles;
    private long totalUsers;
    private long analyzedCount;

    /** Sum of all requested loan amounts (TND). */
    private double totalLoanAmount;

    /** Average AI risk score across analyzed files (0 when none analyzed). */
    private double averageRiskScore;

    /** Number of files still awaiting a final decision (DRAFT or IN_REVIEW). */
    private long pendingCount;

    /** Count of credit files per status (keys are CreditStatus names). */
    private Map<String, Long> statusDistribution;

    /** Count of analyzed credit files per risk level (keys are RiskLevel names). */
    private Map<String, Long> riskLevelDistribution;

    /** Count of credit files per credit type (free-form keys). */
    private Map<String, Long> creditTypeDistribution;

    private List<RecentCreditFile> recentCreditFiles;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentCreditFile {
        private String id;
        private String clientFullName;
        private String creditType;
        private Double loanAmount;
        private String status;
        private String riskLevel;
        private String createdAt;
    }
}
