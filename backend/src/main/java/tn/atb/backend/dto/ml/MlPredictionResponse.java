package tn.atb.backend.dto.ml;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MlPredictionResponse {

    private boolean approved;
    private double riskScore;
    private String riskLevel;
    private String aiDecision;
    private String modelName;

    /** Per-feature attribution of this decision, strongest influence first. */
    private List<MlDecisionFactor> factors;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MlDecisionFactor {
        private String feature;
        private double impact;
        private boolean reducesRisk;
    }
}
