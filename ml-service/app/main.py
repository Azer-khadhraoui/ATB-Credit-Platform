from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.model import model_service
from app.schemas import LoanPredictionRequest, LoanPredictionResponse

app = FastAPI(
    title="ATB Credit Platform — ML Service",
    description="Service d'analyse de risque de crédit, expose le modèle entraîné sur le dataset Loan Prediction.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8081"],
    allow_methods=["POST", "GET"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "model": model_service.model_name}


@app.post("/predict", response_model=LoanPredictionResponse)
def predict(request: LoanPredictionRequest) -> dict:
    try:
        return model_service.predict(request)
    except Exception as exc:  # noqa: BLE001 — surface any preprocessing/model error as a 500 with context
        raise HTTPException(status_code=500, detail=f"Prediction failed: {exc}") from exc
