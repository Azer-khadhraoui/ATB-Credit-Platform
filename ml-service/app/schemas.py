from typing import Literal
from pydantic import BaseModel, Field


class LoanPredictionRequest(BaseModel):
    """Raw features expected by the trained model, matching the Loan Prediction dataset columns."""

    gender: Literal["Male", "Female"]
    married: Literal["Yes", "No"]
    dependents: int = Field(ge=0, le=3)
    education: Literal["Graduate", "Not Graduate"]
    self_employed: Literal["Yes", "No"]
    applicant_income: float = Field(ge=0)
    coapplicant_income: float = Field(ge=0, default=0)
    loan_amount: float = Field(ge=0)
    loan_amount_term: float = Field(ge=1)
    credit_history: Literal[0, 1]
    property_area: Literal["Urban", "Semiurban", "Rural"]


class LoanPredictionResponse(BaseModel):
    approved: bool
    risk_score: float
    risk_level: Literal["LOW", "MEDIUM", "HIGH"]
    ai_decision: Literal["ACCEPTABLE", "RISKY", "REJECTED"]
    model_name: str
