"""Contract and behaviour tests for the credit-risk service.

These run against the real trained model committed under ``models/`` — the same artefact the
container serves — so they catch a broken pipeline, a schema drift, or a model swap that changes
the response shape, not just Python typos.
"""

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


# A well-qualified applicant: graduate, clean credit history, comfortable income, short term.
STRONG_APPLICANT = {
    "gender": "Male",
    "married": "Yes",
    "dependents": 0,
    "education": "Graduate",
    "self_employed": "No",
    "applicant_income": 8000,
    "coapplicant_income": 2000,
    "loan_amount": 100,
    "loan_amount_term": 360,
    "credit_history": 1,
    "property_area": "Urban",
}


def _predict(**overrides):
    payload = {**STRONG_APPLICANT, **overrides}
    return client.post("/predict", json=payload)


def test_health_reports_the_loaded_model():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert body["model"]  # a model name is present


def test_predict_returns_the_full_contract():
    response = _predict()
    assert response.status_code == 200
    body = response.json()

    assert set(body) >= {
        "approved",
        "risk_score",
        "risk_level",
        "ai_decision",
        "model_name",
        "factors",
    }
    assert 0 <= body["risk_score"] <= 100
    assert body["risk_level"] in {"LOW", "MEDIUM", "HIGH"}
    assert body["ai_decision"] in {"ACCEPTABLE", "RISKY", "REJECTED"}


def test_risk_level_matches_the_score_band():
    body = _predict().json()
    score = body["risk_score"]
    expected = "LOW" if score < 30 else "MEDIUM" if score < 70 else "HIGH"
    assert body["risk_level"] == expected


def test_factors_are_ranked_by_absolute_impact():
    factors = _predict().json()["factors"]
    assert factors, "the linear model should expose per-feature factors"

    magnitudes = [abs(f["impact"]) for f in factors]
    assert magnitudes == sorted(magnitudes, reverse=True)

    # The direction flag must agree with the sign of the contribution.
    for factor in factors:
        assert factor["reduces_risk"] == (factor["impact"] > 0)


def test_bad_credit_history_raises_the_risk_score():
    good = _predict(credit_history=1).json()["risk_score"]
    bad = _predict(credit_history=0).json()["risk_score"]
    assert bad > good


def test_rejects_an_unknown_gender():
    response = _predict(gender="Other")
    assert response.status_code == 422


def test_rejects_dependents_above_the_supported_range():
    # The dataset collapses "3+" into 3; anything higher is out of contract.
    response = _predict(dependents=5)
    assert response.status_code == 422
