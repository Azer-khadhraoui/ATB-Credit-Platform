package tn.atb.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Payload sent to the FastAPI ML service. Field names/casing must match the
 * Loan Prediction dataset schema expected by ml-service/app/schemas.py.
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

    @JsonProperty("self_employed")
    private String selfEmployed;

    @JsonProperty("applicant_income")
    private double applicantIncome;

    @JsonProperty("coapplicant_income")
    private double coapplicantIncome;

    @JsonProperty("loan_amount")
    private double loanAmount;

    @JsonProperty("loan_amount_term")
    private double loanAmountTerm;

    @JsonProperty("credit_history")
    private int creditHistory;

    @JsonProperty("property_area")
    private String propertyArea;

    /**
     * Hand-built JSON, bypassing Jackson entirely. Avoids ambiguity between the
     * Jackson 2 and Jackson 3 artifacts both present on this project's classpath.
     */
    public String toJson() {
        return "{"
                + "\"gender\":\"" + gender + "\","
                + "\"married\":\"" + married + "\","
                + "\"dependents\":" + dependents + ","
                + "\"education\":\"" + education + "\","
                + "\"self_employed\":\"" + selfEmployed + "\","
                + "\"applicant_income\":" + applicantIncome + ","
                + "\"coapplicant_income\":" + coapplicantIncome + ","
                + "\"loan_amount\":" + loanAmount + ","
                + "\"loan_amount_term\":" + loanAmountTerm + ","
                + "\"credit_history\":" + creditHistory + ","
                + "\"property_area\":\"" + propertyArea + "\""
                + "}";
    }
}
