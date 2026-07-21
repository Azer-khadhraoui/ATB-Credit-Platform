FROM python:3.14-slim
WORKDIR /app

# System deps needed to build/scikit-learn wheels are already published for this
# image's platform, so a plain pip install is enough — no compiler toolchain required.
COPY requirements-docker.txt .
RUN pip install --no-cache-dir -r requirements-docker.txt

COPY app ./app
COPY models ./models

# Run as a non-root user (DevSecOps: no container process runs as root).
RUN addgroup --system atb && adduser --system --ingroup atb atb
USER atb

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
