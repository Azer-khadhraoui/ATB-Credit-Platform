package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.atb.backend.dto.dashboard.DashboardStatsResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;
import tn.atb.backend.entity.enums.CreditStatus;
import tn.atb.backend.entity.enums.RiskLevel;
import tn.atb.backend.repository.ClientRepository;
import tn.atb.backend.repository.CreditFileRepository;
import tn.atb.backend.repository.UserRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_LIMIT = 6;

    private final ClientRepository clientRepository;
    private final CreditFileRepository creditFileRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getStats() {
        List<CreditFile> creditFiles = creditFileRepository.findAll();

        // Status distribution — seed every status at 0 so the chart always shows all bars.
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (CreditStatus status : CreditStatus.values()) {
            statusDistribution.put(status.name(), 0L);
        }
        for (CreditFile creditFile : creditFiles) {
            if (creditFile.getStatus() != null) {
                statusDistribution.merge(creditFile.getStatus().name(), 1L, Long::sum);
            }
        }

        // Risk level distribution — only analyzed files carry a risk level.
        Map<String, Long> riskLevelDistribution = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            riskLevelDistribution.put(level.name(), 0L);
        }
        long analyzedCount = 0;
        for (CreditFile creditFile : creditFiles) {
            if (creditFile.getRiskLevel() != null) {
                riskLevelDistribution.merge(creditFile.getRiskLevel().name(), 1L, Long::sum);
                analyzedCount++;
            }
        }

        // Credit type distribution — free-form keys, ordered by insertion.
        Map<String, Long> creditTypeDistribution = new LinkedHashMap<>();
        double totalLoanAmount = 0;
        long pendingCount = 0;
        double riskScoreSum = 0;
        for (CreditFile creditFile : creditFiles) {
            if (creditFile.getCreditType() != null) {
                creditTypeDistribution.merge(creditFile.getCreditType(), 1L, Long::sum);
            }
            if (creditFile.getLoanAmount() != null) {
                totalLoanAmount += creditFile.getLoanAmount();
            }
            if (creditFile.getStatus() == CreditStatus.DRAFT || creditFile.getStatus() == CreditStatus.IN_REVIEW) {
                pendingCount++;
            }
            if (creditFile.getRiskScore() != null) {
                riskScoreSum += creditFile.getRiskScore();
            }
        }
        double averageRiskScore = analyzedCount == 0 ? 0 : Math.round((riskScoreSum / analyzedCount) * 100.0) / 100.0;

        Map<String, Client> clientsById = clientRepository
                .findAllById(creditFiles.stream().map(CreditFile::getClientId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Client::getId, Function.identity(), (a, b) -> a));

        List<DashboardStatsResponse.RecentCreditFile> recent = creditFiles.stream()
                .sorted(Comparator.comparing(CreditFile::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_LIMIT)
                .map(creditFile -> {
                    Client client = clientsById.get(creditFile.getClientId());
                    return DashboardStatsResponse.RecentCreditFile.builder()
                            .id(creditFile.getId())
                            .clientFullName(client != null ? client.getFirstName() + " " + client.getLastName() : "—")
                            .creditType(creditFile.getCreditType())
                            .loanAmount(creditFile.getLoanAmount())
                            .status(creditFile.getStatus() != null ? creditFile.getStatus().name() : null)
                            .riskLevel(creditFile.getRiskLevel() != null ? creditFile.getRiskLevel().name() : null)
                            .createdAt(creditFile.getCreatedAt() != null ? creditFile.getCreatedAt().toString() : null)
                            .build();
                })
                .toList();

        return DashboardStatsResponse.builder()
                .totalClients(clientRepository.count())
                .totalCreditFiles(creditFiles.size())
                .totalUsers(userRepository.count())
                .analyzedCount(analyzedCount)
                .totalLoanAmount(totalLoanAmount)
                .averageRiskScore(averageRiskScore)
                .pendingCount(pendingCount)
                .statusDistribution(statusDistribution)
                .riskLevelDistribution(riskLevelDistribution)
                .creditTypeDistribution(creditTypeDistribution)
                .recentCreditFiles(recent)
                .build();
    }
}
