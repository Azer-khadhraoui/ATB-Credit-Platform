package tn.atb.backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Payload sent to the FastAPI ML service. Field names map to the dataset's
 * snake_case columns through the client's naming strategy.
 */
@Getter
@Builder
@AllArgsConstructor
@ToString
public class MlPredictionRequest {

    private String gender;
    private String married;
    private int dependents;
    private String education;
    private String selfEmployed;
    private double applicantIncome;
    private double coapplicantIncome;
    private double loanAmount;
    private double loanAmountTerm;
    private int creditHistory;
    private String propertyArea;
}
