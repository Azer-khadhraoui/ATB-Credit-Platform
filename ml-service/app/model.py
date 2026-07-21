from pathlib import Path

import joblib
import pandas as pd

from app.schemas import LoanPredictionRequest

MODELS_DIR = Path(__file__).resolve().parent.parent / "models"

RAW_TO_TRAINING_COLUMNS = {
    "gender": "Gender",
    "married": "Married",
    "dependents": "Dependents",
    "education": "Education",
    "self_employed": "Self_Employed",
    "applicant_income": "ApplicantIncome",
    "coapplicant_income": "CoapplicantIncome",
    "loan_amount": "LoanAmount",
    "loan_amount_term": "Loan_Amount_Term",
    "credit_history": "Credit_History",
    "property_area": "Property_Area",
}

ENCODED_COLUMNS = ["Gender", "Married", "Education", "Self_Employed", "Property_Area"]


class ModelService:
    def __init__(self) -> None:
        self.model = joblib.load(MODELS_DIR / "model.pkl")
        self.encoders = joblib.load(MODELS_DIR / "encoders.pkl")
        self.feature_columns = joblib.load(MODELS_DIR / "feature_columns.pkl")
        self.model_name = type(self.model).__name__

        # Training-set averages, used to express each feature's contribution as a
        # deviation from a typical applicant. Optional: an explanation is a bonus,
        # never a reason to fail a prediction.
        means_path = MODELS_DIR / "feature_means.pkl"
        self.feature_means = joblib.load(means_path) if means_path.exists() else None

    def _explain(self, df: pd.DataFrame) -> list[dict]:
        """Attributes the decision across features, strongest influence first.

        Only linear models expose per-feature coefficients this directly; for anything
        else we return nothing rather than inventing an explanation.
        """
        if self.feature_means is None or not hasattr(self.model, "coef_"):
            return []

        coefficients = self.model.coef_[0]
        row = df.iloc[0]

        factors = []
        for column, coefficient in zip(self.feature_columns, coefficients):
            impact = float(coefficient * (row[column] - self.feature_means[column]))
            factors.append({
                "feature": column,
                "impact": round(impact, 4),
                "reduces_risk": impact > 0,
            })

        factors.sort(key=lambda f: abs(f["impact"]), reverse=True)
        return factors

    def predict(self, request: LoanPredictionRequest) -> dict:
        row = {
            RAW_TO_TRAINING_COLUMNS[field]: value
            for field, value in request.model_dump().items()
        }
        df = pd.DataFrame([row])

        for column in ENCODED_COLUMNS:
            df[column] = self.encoders[column].transform(df[column])

        df = df[self.feature_columns]

        approved = bool(self.model.predict(df)[0])
        probabilities = self.model.predict_proba(df)[0]
        approval_probability = float(probabilities[1])
        risk_score = round((1 - approval_probability) * 100, 2)

        if risk_score < 30:
            risk_level = "LOW"
        elif risk_score < 70:
            risk_level = "MEDIUM"
        else:
            risk_level = "HIGH"

        if not approved:
            ai_decision = "REJECTED"
        elif risk_score >= 50:
            ai_decision = "RISKY"
        else:
            ai_decision = "ACCEPTABLE"

        return {
            "approved": approved,
            "risk_score": risk_score,
            "risk_level": risk_level,
            "ai_decision": ai_decision,
            "model_name": self.model_name,
            "factors": self._explain(df),
        }


model_service = ModelService()
