package tn.atb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.atb.backend.entity.enums.AIDecision;
import tn.atb.backend.entity.enums.RiskLevel;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictionResult {

    private Double riskScore;

    private RiskLevel riskLevel;

    private AIDecision aiDecision;

    private Double confidence;

    private String modelName;

    private LocalDateTime predictedAt;
}
