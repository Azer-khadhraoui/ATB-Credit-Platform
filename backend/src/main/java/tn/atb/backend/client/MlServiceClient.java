package tn.atb.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.atb.backend.dto.ml.MlPredictionRequest;
import tn.atb.backend.dto.ml.MlPredictionResponse;
import tn.atb.backend.exception.BadRequestException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Talks to the FastAPI ML service using the JDK HttpClient and hand-rolled JSON.
 * Spring's RestClient proved unreliable here: this project carries two Jackson major
 * versions on its classpath (Jackson 2 via jjwt, Jackson 3 via Spring Boot 4.1), which
 * left RestClient sending empty request bodies. Sending a raw JSON string over the JDK
 * client removes every message-converter/content-length ambiguity.
 */
@Slf4j
@Component
public class MlServiceClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            // Force HTTP/1.1: the JDK client otherwise sends an HTTP/2 (h2c) upgrade header
            // that uvicorn rejects ("Unsupported upgrade request"), dropping the request body.
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String mlServiceUrl;

    public MlServiceClient(@Value("${ml.service.url}") String mlServiceUrl) {
        this.mlServiceUrl = mlServiceUrl;
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {
        String requestJson = request.toJson();
        log.info("ML request payload: {}", requestJson);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(mlServiceUrl + "/predict"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("ML service call failed", ex);
            throw new BadRequestException(
                    "Le service d'analyse IA est indisponible. Vérifiez qu'il est démarré (uvicorn app.main:app --port 8000).");
        }

        if (response.statusCode() >= 400) {
            log.error("ML service rejected the request ({}): {}", response.statusCode(), response.body());
            throw new BadRequestException(
                    "Le service d'analyse IA a rejeté la requête (" + response.statusCode() + "): " + response.body());
        }

        return parseResponse(response.body());
    }

    private MlPredictionResponse parseResponse(String json) {
        MlPredictionResponse response = new MlPredictionResponse();
        response.setApproved(Boolean.parseBoolean(extract(json, "approved", "(true|false)")));
        response.setRiskScore(Double.parseDouble(extract(json, "risk_score", "([0-9.]+)")));
        response.setRiskLevel(extract(json, "risk_level", "\"([^\"]*)\""));
        response.setAiDecision(extract(json, "ai_decision", "\"([^\"]*)\""));
        response.setModelName(extract(json, "model_name", "\"([^\"]*)\""));
        return response;
    }

    private String extract(String json, String field, String valuePattern) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*" + valuePattern).matcher(json);
        if (!matcher.find()) {
            throw new BadRequestException("Réponse inattendue du service d'analyse IA (champ '" + field + "' manquant).");
        }
        return matcher.group(matcher.groupCount());
    }
}
