package tn.atb.backend.entity.enums;

/**
 * Type of area the client's property is located in. Maps to the Loan Prediction
 * dataset's Property_Area feature, which the risk model consumes directly.
 */
public enum PropertyArea {
    URBAN,
    SEMIURBAN,
    RURAL
}
