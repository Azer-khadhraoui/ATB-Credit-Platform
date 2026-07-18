# Dataset — Loan Prediction (Kaggle)

Source: https://www.kaggle.com/datasets/altruistdelhite04/loan-prediction-problem-dataset

- `train.csv` — labeled training set (614 rows), includes `Loan_Status` (target).
- `test.csv` — unlabeled evaluation set, no `Loan_Status` column.

## Columns

| Column | Description |
|---|---|
| Loan_ID | Unique loan application ID |
| Gender | Male / Female |
| Married | Applicant married (Y/N) |
| Dependents | Number of dependents |
| Education | Graduate / Not Graduate |
| Self_Employed | Y/N |
| ApplicantIncome | Applicant income |
| CoapplicantIncome | Co-applicant income |
| LoanAmount | Loan amount (in thousands) |
| Loan_Amount_Term | Term of loan (in months) |
| Credit_History | Meets credit history guidelines (1/0) |
| Property_Area | Urban / Semiurban / Rural |
| Loan_Status | Y/N — approved or not (train.csv only, this is the prediction target) |
