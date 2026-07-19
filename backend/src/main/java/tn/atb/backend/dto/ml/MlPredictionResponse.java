package tn.atb.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MlPredictionResponse {

    private boolean approved;

    @JsonProperty("risk_score")
    private double riskScore;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("ai_decision")
    private String aiDecision;

    @JsonProperty("model_name")
    private String modelName;
}
